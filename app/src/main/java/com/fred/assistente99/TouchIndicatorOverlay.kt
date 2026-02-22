package com.fred.assistente99

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color

/**
 * Overlay para mostrar indicador visual de toque
 */
object TouchIndicatorOverlay {
    
    private var windowManager: WindowManager? = null
    private var indicatorView: IndicatorView? = null
    private val handler = Handler(Looper.getMainLooper())
    
    @SuppressLint("ClickableViewAccessibility")
    fun show(context: Context, x: Float, y: Float, size: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(context)) {
            return
        }
        
        // Remover indicador anterior
        hide()
        
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        val params = WindowManager.LayoutParams(
            size,
            size,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            else 
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        
        params.gravity = Gravity.TOP or Gravity.START
        params.x = x.toInt() - size / 2
        params.y = y.toInt() - size / 2
        
        indicatorView = IndicatorView(context, size)
        
        try {
            windowManager?.addView(indicatorView, params)
            
            // Remover após 500ms
            handler.postDelayed({
                hide()
            }, 500)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun hide() {
        try {
            if (indicatorView != null) {
                windowManager?.removeView(indicatorView)
                indicatorView = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    class IndicatorView(context: Context, private val size: Int) : View(context) {
        
        private val paint = Paint().apply {
            color = Color.parseColor("#80FF0000") // Vermelho translúcido
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        
        private val strokePaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val radius = size / 2f
            canvas.drawCircle(radius, radius, radius - 4f, paint)
            canvas.drawCircle(radius, radius, radius - 4f, strokePaint)
        }
    }
}
