package com.fred.assistente99

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * View arrastável para calibração de posição de toque
 */
class DraggableTargetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var targetX: Float = 0f
    private var targetY: Float = 0f
    private var targetNumber: Int = 1
    private var onPositionChanged: ((Float, Float) -> Unit)? = null

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
        textSize = 40f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDragging = false

    fun setTargetNumber(number: Int) {
        targetNumber = number
        invalidate()
    }

    fun setPosition(x: Float, y: Float) {
        targetX = x
        targetY = y
        invalidate()
    }

    fun setOnPositionChangedListener(listener: (Float, Float) -> Unit) {
        onPositionChanged = listener
    }

    fun getPositionX(): Float = targetX
    fun getPositionY(): Float = targetY

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val radius = 50f
        
        // Desenhar círculo preenchido
        canvas.drawCircle(targetX, targetY, radius, targetPaint)
        
        // Desenhar borda
        canvas.drawCircle(targetX, targetY, radius, strokePaint)
        
        // Desenhar número
        canvas.drawText(targetNumber.toString(), targetX, targetY + 15f, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val dx = event.x - targetX
                val dy = event.y - targetY
                if (dx * dx + dy * dy <= 50f * 50f) {
                    isDragging = true
                    lastTouchX = event.x
                    lastTouchY = event.y
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    targetX += dx
                    targetY += dy
                    
                    // Limitar aos limites da view
                    targetX = targetX.coerceIn(50f, width - 50f)
                    targetY = targetY.coerceIn(50f, height - 50f)
                    
                    lastTouchX = event.x
                    lastTouchY = event.y
                    invalidate()
                    
                    onPositionChanged?.invoke(targetX, targetY)
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
