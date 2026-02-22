package com.fred.assistente99

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CalibrationActivity : AppCompatActivity() {

    private lateinit var calibrationView: CalibrationView
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
        calibrationView = findViewById(R.id.calibrationView)
        tvInstructions = findViewById(R.id.tvInstructions)
        btnSave = findViewById(R.id.btnSaveCalibration)
        btnCancel = findViewById(R.id.btnCancelCalibration)
    }

    private fun loadSavedPositions() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        
        // Valores padrão
        val defaultY = (screenHeight * 0.85).toFloat()
        val centerX = screenWidth / 2f
        
        val x1 = ConfigManager.getAlvo1X(this)
        val y1 = ConfigManager.getAlvo1Y(this)
        val x2 = ConfigManager.getAlvo2X(this)
        val y2 = ConfigManager.getAlvo2Y(this)
        val x3 = ConfigManager.getAlvo3X(this)
        val y3 = ConfigManager.getAlvo3Y(this)
        
        calibrationView.setPositions(
            if (x1 > 0) x1.toFloat() else centerX - 30f,
            if (y1 > 0) y1.toFloat() else defaultY,
            if (x2 > 0) x2.toFloat() else centerX,
            if (y2 > 0) y2.toFloat() else defaultY + 10f,
            if (x3 > 0) x3.toFloat() else centerX + 30f,
            if (y3 > 0) y3.toFloat() else defaultY - 5f
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
        val (t1, t2, t3) = calibrationView.getPositions()
        
        ConfigManager.setAlvo1(this, t1.first.toInt(), t1.second.toInt())
        ConfigManager.setAlvo2(this, t2.first.toInt(), t2.second.toInt())
        ConfigManager.setAlvo3(this, t3.first.toInt(), t3.second.toInt())
        
        Toast.makeText(this, "✓ Posições salvas!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
