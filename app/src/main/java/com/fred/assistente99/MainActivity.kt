package com.fred.assistente99

import android.content.Intent
import android.net.Uri
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
    private lateinit var btnSalvarConfig: Button
    private lateinit var tvAceitas: TextView
    private lateinit var tvRejeitadas: TextView
    private lateinit var tvUltimaCorrida: TextView
    
    // Configurações de corrida
    private lateinit var etValorMinimoKm: EditText
    private lateinit var etDistanciaMaximaBusca: EditText
    private lateinit var switchAutoAceitar: Switch
    
    // Configurações de toque
    private lateinit var etQuantidadeToques: EditText
    private lateinit var etDuracaoToque: EditText
    private lateinit var etIntervaloToques: EditText
    private lateinit var etTamanhoArea: EditText
    private lateinit var etPosicaoY: EditText
    private lateinit var switchMostrarIndicador: Switch

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
        btnSalvarConfig = findViewById(R.id.btnSalvarConfig)
        tvAceitas = findViewById(R.id.tvAceitas)
        tvRejeitadas = findViewById(R.id.tvRejeitadas)
        tvUltimaCorrida = findViewById(R.id.tvUltimaCorrida)
        
        // Configurações de corrida
        etValorMinimoKm = findViewById(R.id.etValorMinimoKm)
        etDistanciaMaximaBusca = findViewById(R.id.etDistanciaMaximaBusca)
        switchAutoAceitar = findViewById(R.id.switchAutoAceitar)
        
        // Configurações de toque
        etQuantidadeToques = findViewById(R.id.etQuantidadeToques)
        etDuracaoToque = findViewById(R.id.etDuracaoToque)
        etIntervaloToques = findViewById(R.id.etIntervaloToques)
        etTamanhoArea = findViewById(R.id.etTamanhoArea)
        etPosicaoY = findViewById(R.id.etPosicaoY)
        switchMostrarIndicador = findViewById(R.id.switchMostrarIndicador)
    }

    private fun carregarConfiguracoes() {
        // Configurações de corrida
        etValorMinimoKm.setText(String.format("%.2f", ConfigManager.getValorMinimoKm(this)))
        etDistanciaMaximaBusca.setText(String.format("%.1f", ConfigManager.getDistanciaMaximaBusca(this)))
        switchAutoAceitar.isChecked = ConfigManager.isAutoAceitar(this)
        
        // Configurações de toque
        etQuantidadeToques.setText(ConfigManager.getQuantidadeToques(this).toString())
        etDuracaoToque.setText(ConfigManager.getDuracaoToque(this).toString())
        etIntervaloToques.setText(ConfigManager.getIntervaloToques(this).toString())
        etTamanhoArea.setText(ConfigManager.getTamanhoAreaToque(this).toString())
        etPosicaoY.setText(ConfigManager.getPosicaoYToque(this).toString())
        switchMostrarIndicador.isChecked = ConfigManager.isMostrarIndicador(this)
    }

    private fun setupListeners() {
        btnToggleServico.setOnClickListener {
            if (ConfigManager.isServicoAtivo(this)) {
                desativarServico()
            } else {
                ativarServico()
            }
        }
        
        btnSalvarConfig.setOnClickListener {
            salvarConfiguracoes()
        }
    }

    private fun ativarServico() {
        // Verificar permissão de overlay
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            Toast.makeText(this, "Permita sobreposição de tela para o assistente", Toast.LENGTH_LONG).show()
            return
        }
        
        // Verificar se accessibility service está ativo
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(this, "Ative o serviço de acessibilidade nas configurações", Toast.LENGTH_LONG).show()
            abrirConfiguracoesAcessibilidade()
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
            // Configurações de corrida
            val valorMinimo = etValorMinimoKm.text.toString().replace(",", ".").toDouble()
            val distanciaMaxima = etDistanciaMaximaBusca.text.toString().replace(",", ".").toDouble()
            
            ConfigManager.setValorMinimoKm(this, valorMinimo)
            ConfigManager.setDistanciaMaximaBusca(this, distanciaMaxima)
            ConfigManager.setAutoAceitar(this, switchAutoAceitar.isChecked)
            
            // Configurações de toque
            val quantidadeToques = etQuantidadeToques.text.toString().toIntOrNull() ?: 3
            val duracaoToque = etDuracaoToque.text.toString().toIntOrNull() ?: 150
            val intervaloToques = etIntervaloToques.text.toString().toIntOrNull() ?: 100
            val tamanhoArea = etTamanhoArea.text.toString().toIntOrNull() ?: 100
            val posicaoY = etPosicaoY.text.toString().toIntOrNull() ?: 85
            
            ConfigManager.setQuantidadeToques(this, quantidadeToques)
            ConfigManager.setDuracaoToque(this, duracaoToque)
            ConfigManager.setIntervaloToques(this, intervaloToques)
            ConfigManager.setTamanhoAreaToque(this, tamanhoArea)
            ConfigManager.setPosicaoYToque(this, posicaoY)
            ConfigManager.setMostrarIndicador(this, switchMostrarIndicador.isChecked)
            
            Toast.makeText(this, "Configurações salvas!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao salvar configurações: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun atualizarUI() {
        val servicoAtivo = ConfigManager.isServicoAtivo(this)
        
        if (servicoAtivo) {
            tvStatus.text = getString(R.string.assistente_ativo)
            tvStatus.setTextColor(getColor(R.color.green_700))
            btnToggleServico.text = getString(R.string.btn_desativar_servico)
            btnToggleServico.setBackgroundColor(getColor(R.color.red_500))
        } else {
            tvStatus.text = getString(R.string.assistente_inativo)
            tvStatus.setTextColor(getColor(R.color.red_500))
            btnToggleServico.text = getString(R.string.btn_ativar_servico)
            btnToggleServico.setBackgroundColor(getColor(R.color.green_700))
        }
        
        // Atualizar estatísticas
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
