package com.fred.assistente99

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView

/**
 * Serviço para exibir ícone flutuante sobre outros apps
 */
class FloatingOverlayService : Service() {

    companion object {
        @Volatile
        var instance: FloatingOverlayService? = null
            private set
    }

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var isExpanded = false

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Verificar permissão de overlay
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        // Layout params para o overlay
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            else 
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 200
        
        // Inflar o layout do overlay
        floatingView = LayoutInflater.from(this).inflate(R.layout.overlay_floating, null)
        
        val iconView = floatingView?.findViewById<ImageView>(R.id.ivOverlayIcon)
        
        // Configurar arrastar
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var lastTapTime = 0L
        
        floatingView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(floatingView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val currentTime = System.currentTimeMillis()
                    val dx = kotlin.math.abs(event.rawX - initialTouchX)
                    val dy = kotlin.math.abs(event.rawY - initialTouchY)
                    
                    // Se foi um toque simples (não arrastar)
                    if (dx < 10 && dy < 10) {
                        // Duplo toque para abrir configurações
                        if (currentTime - lastTapTime < 300) {
                            abrirMainActivity()
                        }
                        lastTapTime = currentTime
                    }
                    true
                }
                else -> false
            }
        }
        
        // Atualizar cor do ícone baseado no status
        atualizarIcone()
        
        windowManager?.addView(floatingView, params)
    }

    private fun abrirMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun atualizarStatus() {
        atualizarIcone()
    }

    private fun atualizarIcone() {
        val iconView = floatingView?.findViewById<ImageView>(R.id.ivOverlayIcon)
        val servicoAtivo = ConfigManager.isServicoAtivo(applicationContext)
        
        if (servicoAtivo) {
            iconView?.setBackgroundResource(R.drawable.overlay_bg_green)
        } else {
            iconView?.setBackgroundResource(R.drawable.overlay_bg_gray)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (floatingView != null) {
            windowManager?.removeView(floatingView)
        }
        instance = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
