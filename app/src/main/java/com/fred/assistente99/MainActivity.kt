package com.fred.assistente99

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnToggleServico: Button
    private lateinit var btnCalibrar: Button
    private lateinit var etValorMinimoKm: EditText
    private lateinit var etDistanciaMaximaBusca: EditText
    private lateinit var switchAutoAceitar: Switch
    private lateinit var switchMostrarIndicador: Switch
    private lateinit var etDuracaoToque: EditText
    private lateinit var etIntervaloMin: EditText
    private lateinit var etIntervaloMax: EditText
    private lateinit var etDelayInicioMin: EditText
    private lateinit var etDelayInicioMax: EditText
    private lateinit var etTamanhoArea: EditText
    private lateinit var btnSalvarConfig: Button
    private lateinit var tvAceitas: TextView
    private lateinit var tvRejeitadas: TextView
    private lateinit var tvUltimaCorrida: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        carregarConfiguracoes()
        setupListeners()
        atualizarUI()
    }

    override fun onResume() {
        super.onResume()
        atualizarUI()
    }

    private fun initViews() {
        tvStatus = findViewById(R.id.tvStatus)
        btnToggleServico = findViewById(R.id.btnToggleServico)
        btnCalibrar = findViewById(R.id.btnCalibrar)
        etValorMinimoKm = findViewById(R.id.etValorMinimoKm)
        etDistanciaMaximaBusca = findViewById(R.id.etDistanciaMaximaBusca)
        switchAutoAceitar = findViewById(R.id.switchAutoAceitar)
        switchMostrarIndicador = findViewById(R.id.switchMostrarIndicador)
        etDuracaoToque = findViewById(R.id.etDuracaoToque)
        etIntervaloMin = findViewById(R.id.etIntervaloMin)
        etIntervaloMax = findViewById(R.id.etIntervaloMax)
        etDelayInicioMin = findViewById(R.id.etDelayInicioMin)
        etDelayInicioMax = findViewById(R.id.etDelayInicioMax)
        etTamanhoArea = findViewById(R.id.etTamanhoArea)
        btnSalvarConfig = findViewById(R.id.btnSalvarConfig)
        tvAceitas = findViewById(R.id.tvAceitas)
        tvRejeitadas = findViewById(R.id.tvRejeitadas)
        tvUltimaCorrida = findViewById(R.id.tvUltimaCorrida)
    }

    private fun carregarConfiguracoes() {
        etValorMinimoKm.setText(String.format("%.2f", ConfigManager.getValorMinimoKm(this)))
        etDistanciaMaximaBusca.setText(String.format("%.1f", ConfigManager.getDistanciaMaximaBusca(this)))
        switchAutoAceitar.isChecked = ConfigManager.isAutoAceitar(this)
        switchMostrarIndicador.isChecked = ConfigManager.isMostrarIndicador(this)
        etDuracaoToque.setText(ConfigManager.getDuracaoToque(this).toString())
        etIntervaloMin.setText(ConfigManager.getIntervaloMin(this).toString())
        etIntervaloMax.setText(ConfigManager.getIntervaloMax(this).toString())
        etDelayInicioMin.setText(ConfigManager.getDelayInicioMin(this).toString())
        etDelayInicioMax.setText(ConfigManager.getDelayInicioMax(this).toString())
        etTamanhoArea.setText(ConfigManager.getTamanhoAreaToque(this).toString())
    }

    private fun setupListeners() {
        btnToggleServico.setOnClickListener {
            if (ConfigManager.isServicoAtivo(this)) {
                desativarServico()
            } else {
                ativarServico()
            }
        }
        
        btnCalibrar.setOnClickListener {
            startActivity(Intent(this, CalibrationActivity::class.java))
        }
        
        btnSalvarConfig.setOnClickListener {
            salvarConfiguracoes()
        }
    }

    private fun ativarServico() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            Toast.makeText(this, "Permita sobreposição de tela para o assistente", Toast.LENGTH_LONG).show()
            return
        }
        
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(this, "Ative o serviço de acessibilidade nas configurações", Toast.LENGTH_LONG).show()
            abrirConfiguracoesAcessibilidade()
            return
        }
        
        if (!ConfigManager.isAlvosConfigurados(this)) {
            Toast.makeText(this, "Configure os alvos de toque primeiro!", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, CalibrationActivity::class.java))
            return
        }
        
        ConfigManager.setServicoAtivo(this, true)
        atualizarUI()
        Toast.makeText(this, "Assistente ativado!", Toast.LENGTH_SHORT).show()
    }

    private fun desativarServico() {
        ConfigManager.setServicoAtivo(this, false)
        atualizarUI()
        Toast.makeText(this, "Assistente desativado", Toast.LENGTH_SHORT).show()
    }

    private fun salvarConfiguracoes() {
        try {
            val valorMinimo = etValorMinimoKm.text.toString().replace(",", ".").toDouble()
            val distanciaMaxima = etDistanciaMaximaBusca.text.toString().replace(",", ".").toDouble()
            
            ConfigManager.setValorMinimoKm(this, valorMinimo)
            ConfigManager.setDistanciaMaximaBusca(this, distanciaMaxima)
            ConfigManager.setAutoAceitar(this, switchAutoAceitar.isChecked)
            ConfigManager.setMostrarIndicador(this, switchMostrarIndicador.isChecked)
            ConfigManager.setDuracaoToque(this, etDuracaoToque.text.toString().toInt())
            ConfigManager.setIntervaloMin(this, etIntervaloMin.text.toString().toInt())
            ConfigManager.setIntervaloMax(this, etIntervaloMax.text.toString().toInt())
            ConfigManager.setDelayInicioMin(this, etDelayInicioMin.text.toString().toInt())
            ConfigManager.setDelayInicioMax(this, etDelayInicioMax.text.toString().toInt())
            ConfigManager.setTamanhoAreaToque(this, etTamanhoArea.text.toString().toInt())
            
            Toast.makeText(this, "Configurações salvas!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao salvar configurações", Toast.LENGTH_SHORT).show()
        }
    }

    private fun atualizarUI() {
        val servicoAtivo = ConfigManager.isServicoAtivo(this)
        val alvosConfigurados = ConfigManager.isAlvosConfigurados(this)
        
        if (servicoAtivo) {
            tvStatus.text = "✓ Assistente Ativo"
            tvStatus.setTextColor(getColor(R.color.green_700))
            btnToggleServico.text = "Desativar Assistente"
            btnToggleServico.setBackgroundColor(getColor(R.color.red_500))
        } else {
            tvStatus.text = if (alvosConfigurados) "Assistente Inativo" else "Configure os alvos primeiro!"
            tvStatus.setTextColor(if (alvosConfigurados) getColor(R.color.red_500) else getColor(R.color.orange_500))
            btnToggleServico.text = "Ativar Assistente"
            btnToggleServico.setBackgroundColor(getColor(R.color.green_700))
        }
        
        tvAceitas.text = ConfigManager.getCorridasAceitas(this).toString()
        tvRejeitadas.text = ConfigManager.getCorridasRejeitadas(this).toString()
        tvUltimaCorrida.text = ConfigManager.getUltimaCorrida(this)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        var accessibilityEnabled = 0
        val serviceName = packageName + "/" + CorridaAccessibilityService::class.java.canonicalName
        
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {
            return false
        }
        
        if (accessibilityEnabled == 1) {
            val colonSplitter = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            
            val splitter = colonSplitter.split(":").toTypedArray()
            for (service in splitter) {
                if (service.equals(serviceName, ignoreCase = true)) {
                    return true
                }
            }
        }
        
        return false
    }

    private fun abrirConfiguracoesAcessibilidade() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
}
