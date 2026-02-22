package com.fred.assistente99

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CalibrationActivity : AppCompatActivity() {

    private lateinit var targetView1: DraggableTargetView
    private lateinit var targetView2: DraggableTargetView
    private lateinit var targetView3: DraggableTargetView
    private lateinit var tvInstructions: TextView
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calibration)
        
        initViews()
        loadSavedPositions()
        setupListeners()
    }

    private fun initViews() {
        targetView1 = findViewById(R.id.targetView1)
        targetView2 = findViewById(R.id.targetView2)
        targetView3 = findViewById(R.id.targetView3)
        tvInstructions = findViewById(R.id.tvInstructions)
        btnSave = findViewById(R.id.btnSaveCalibration)
        btnCancel = findViewById(R.id.btnCancelCalibration)
        
        targetView1.setTargetNumber(1)
        targetView2.setTargetNumber(2)
        targetView3.setTargetNumber(3)
    }

    private fun loadSavedPositions() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        
        // Valores padrão no centro inferior da tela
        val defaultY = (screenHeight * 0.85).toInt()
        val centerX = screenWidth / 2
        
        val x1 = ConfigManager.getAlvo1X(this)
        val y1 = ConfigManager.getAlvo1Y(this)
        val x2 = ConfigManager.getAlvo2X(this)
        val y2 = ConfigManager.getAlvo2Y(this)
        val x3 = ConfigManager.getAlvo3X(this)
        val y3 = ConfigManager.getAlvo3Y(this)
        
        targetView1.setPosition(
            if (x1 > 0) x1.toFloat() else (centerX - 20).toFloat(),
            if (y1 > 0) y1.toFloat() else defaultY.toFloat()
        )
        targetView2.setPosition(
            if (x2 > 0) x2.toFloat() else centerX.toFloat(),
            if (y2 > 0) y2.toFloat() else (defaultY + 5).toFloat()
        )
        targetView3.setPosition(
            if (x3 > 0) x3.toFloat() else (centerX + 20).toFloat(),
            if (y3 > 0) y3.toFloat() else (defaultY - 5).toFloat()
        )
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            saveCalibration()
        }
        
        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveCalibration() {
        ConfigManager.setAlvo1(this, targetView1.getPositionX().toInt(), targetView1.getPositionY().toInt())
        ConfigManager.setAlvo2(this, targetView2.getPositionX().toInt(), targetView2.getPositionY().toInt())
        ConfigManager.setAlvo3(this, targetView3.getPositionX().toInt(), targetView3.getPositionY().toInt())
        
        Toast.makeText(this, " Posições salvas!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
