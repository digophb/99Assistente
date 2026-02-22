package com.fred.assistente99

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt

/**
 * View única para calibração dos 3 alvos de toque
 */
class CalibrationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private data class Target(var x: Float, var y: Float, val number: Int)

    private val targets = mutableListOf(
        Target(0f, 0f, 1),
        Target(0f, 0f, 2),
        Target(0f, 0f, 3)
    )

    private var draggedTarget: Target? = null
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private val targetPaint = Paint().apply {
        color = Color.parseColor("#CCFF0000")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val strokePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    private var initialized = false
    private var onPositionsChanged: (() -> Unit)? = null

    fun setOnPositionsChangedListener(listener: () -> Unit) {
        onPositionsChanged = listener
    }

    fun setPositions(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        targets[0].x = x1
        targets[0].y = y1
        targets[1].x = x2
        targets[1].y = y2
        targets[2].x = x3
        targets[2].y = y3
        initialized = true
        invalidate()
    }

    fun getPositions(): Triple<Pair<Float, Float>, Pair<Float, Float>, Pair<Float, Float>> {
        return Triple(
            Pair(targets[0].x, targets[0].y),
            Pair(targets[1].x, targets[1].y),
            Pair(targets[2].x, targets[2].y)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        if (!initialized) {
            // Posição padrão: centro da tela
            val centerY = h / 2f
            val centerX = w / 2f
            
            targets[0].x = centerX - 40f
            targets[0].y = centerY
            targets[1].x = centerX
            targets[1].y = centerY + 10f
            targets[2].x = centerX + 40f
            targets[2].y = centerY - 5f
            
            initialized = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val radius = 45f
        
        for (target in targets) {
            // Desenhar círculo preenchido
            canvas.drawCircle(target.x, target.y, radius, targetPaint)
            
            // Desenhar borda
            canvas.drawCircle(target.x, target.y, radius, strokePaint)
            
            // Desenhar número
            canvas.drawText(target.number.toString(), target.x, target.y + 12f, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                for (target in targets) {
                    val dx = event.x - target.x
                    val dy = event.y - target.y
                    if (dx * dx + dy * dy <= 45f * 45f) {
                        draggedTarget = target
                        lastTouchX = event.x
                        lastTouchY = event.y
                        return true
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (draggedTarget != null) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    draggedTarget!!.x += dx
                    draggedTarget!!.y += dy
                    
                    // Limitar aos limites da view
                    draggedTarget!!.x = draggedTarget!!.x.coerceIn(45f, width - 45f)
                    draggedTarget!!.y = draggedTarget!!.y.coerceIn(45f, height - 45f)
                    
                    lastTouchX = event.x
                    lastTouchY = event.y
                    invalidate()
                    onPositionsChanged?.invoke()
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                draggedTarget = null
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
