package com.fred.assistente99

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.regex.Pattern

/**
 * Accessibility Service para monitorar e automatizar o 99 Motorista
 */
class CorridaAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "99Assistente"
        private const val PACKAGE_99 = "com.taxis99"
        
        // Padrões regex para extrair valores
        private val VALOR_PATTERN = Pattern.compile("R\\$\\s*([\\d.,]+)", Pattern.CASE_INSENSITIVE)
        private val VALOR_KM_PATTERN = Pattern.compile("R\\$\\s*([\\d.,]+)\\s*/\\s*km", Pattern.CASE_INSENSITIVE)
        private val KM_PATTERN = Pattern.compile("([\\d.,]+)\\s*km", Pattern.CASE_INSENSITIVE)
        
        @Volatile
        var instance: CorridaAccessibilityService? = null
            private set
    }

    private val handler = Handler(Looper.getMainLooper())
    private var ultimaCorridaProcessada: String = ""
    private var tempoUltimaCorrida: Long = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Accessibility Service conectado")
        
        // Iniciar overlay flutuante
        startService(Intent(this, FloatingOverlayService::class.java))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        // Verificar se é o app do 99
        if (event.packageName?.toString() != PACKAGE_99) return
        
        // Verificar tipo de evento
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                verificarCorridaDisponivel()
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service interrompido")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        stopService(Intent(this, FloatingOverlayService::class.java))
    }

    private fun verificarCorridaDisponivel() {
        val rootNode = rootInActiveWindow ?: return
        
        try {
            // Procurar por elementos que indicam uma corrida disponível
            val infoCorrida = extrairInfoCorrida(rootNode)
            
            if (infoCorrida != null && isNovaCorrida(infoCorrida)) {
                processarCorrida(infoCorrida)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar corrida", e)
        } finally {
            rootNode.recycle()
        }
    }

    private fun extrairInfoCorrida(rootNode: AccessibilityNodeInfo): InfoCorrida? {
        // Procurar por textos que contenham valores em R$
        val nodesComTexto = rootNode.findAccessibilityNodeInfosByViewId("android:id/content")
        
        var valorTotal: Double? = null
        var valorPorKm: Double? = null
        var distanciaBusca: Double? = null
        var distanciaCorrida: Double? = null
        
        // Buscar todos os textos na tela
        val textos = mutableListOf<String>()
        buscarTextosRecursivo(rootNode, textos)
        
        Log.d(TAG, "Textos encontrados: $textos")
        
        for (texto in textos) {
            // Extrair valor por km (formato: R$ X,XX/km)
            val matcherKm = VALOR_KM_PATTERN.matcher(texto)
            if (matcherKm.find()) {
                valorPorKm = parseValor(matcherKm.group(1))
            }
            
            // Extrair valor total (formato: R$ XX,XX sem /km)
            val matcherValor = VALOR_PATTERN.matcher(texto)
            if (matcherValor.find() && !texto.contains("/km", ignoreCase = true)) {
                valorTotal = parseValor(matcherValor.group(1))
            }
            
            // Extrair distâncias
            val matcherDistancia = KM_PATTERN.matcher(texto)
            var primeiroKm = true
            while (matcherDistancia.find()) {
                val km = parseValor(matcherDistancia.group(1))
                if (primeiroKm) {
                    distanciaBusca = km // Primeiro km geralmente é até o passageiro
                    primeiroKm = false
                } else {
                    distanciaCorrida = km // Segundo km é a distância da corrida
                }
            }
        }
        
        // Se encontrou valor por km, temos uma corrida
        if (valorPorKm != null) {
            return InfoCorrida(
                valorTotal = valorTotal ?: 0.0,
                valorPorKm = valorPorKm,
                distanciaBusca = distanciaBusca ?: 0.0,
                distanciaCorrida = distanciaCorrida ?: 0.0,
                textoCompleto = textos.joinToString(" | ")
            )
        }
        
        return null
    }

    private fun buscarTextosRecursivo(node: AccessibilityNodeInfo, textos: MutableList<String>) {
        if (node.text != null && node.text.toString().isNotBlank()) {
            textos.add(node.text.toString())
        }
        if (node.contentDescription != null && node.contentDescription.toString().isNotBlank()) {
            textos.add(node.contentDescription.toString())
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            buscarTextosRecursivo(child, textos)
            child.recycle()
        }
    }

    private fun parseValor(valorStr: String?): Double? {
        if (valorStr.isNullOrBlank()) return null
        return try {
            // Converter formato brasileiro (1.234,56) para double
            val limpo = valorStr.replace(".", "").replace(",", ".").trim()
            limpo.toDouble()
        } catch (e: Exception) {
            null
        }
    }

    private fun isNovaCorrida(info: InfoCorrida): Boolean {
        val agora = System.currentTimeMillis()
        val identificador = "${info.valorPorKm}_${info.distanciaCorrida}"
        
        // Evitar processar a mesma corrida múltiplas vezes (dentro de 5 segundos)
        if (identificador == ultimaCorridaProcessada && (agora - tempoUltimaCorrida) < 5000) {
            return false
        }
        
        ultimaCorridaProcessada = identificador
        tempoUltimaCorrida = agora
        return true
    }

    private fun processarCorrida(info: InfoCorrida) {
        Log.d(TAG, "Processando corrida: $info")
        
        val valorMinimo = ConfigManager.getValorMinimoKm(applicationContext)
        val distanciaMaxima = ConfigManager.getDistanciaMaximaBusca(applicationContext)
        val autoAceitar = ConfigManager.isAutoAceitar(applicationContext)
        
        val deveAceitar = info.valorPorKm >= valorMinimo && 
                         info.distanciaBusca <= distanciaMaxima
        
        val mensagem = "R$${"%.2f".format(info.valorTotal)} | ${"%.2f".format(info.valorPorKm)}/km | Busca: ${"%.1f".format(info.distanciaBusca)}km"
        ConfigManager.setUltimaCorrida(applicationContext, mensagem)
        
        if (deveAceitar) {
            Log.d(TAG, "ACEITANDO corrida: $mensagem")
            ConfigManager.incrementarCorridasAceitas(applicationContext)
            
            if (autoAceitar) {
                // Aceitar com pequeno delay para garantir que a tela está pronta
                handler.postDelayed({ clicarAceitar() }, 200)
            }
        } else {
            Log.d(TAG, "REJEITANDO corrida: $mensagem")
            ConfigManager.incrementarCorridasRejeitadas(applicationContext)
            
            if (autoAceitar) {
                // Rejeitar com pequeno delay
                handler.postDelayed({ clicarRejeitar() }, 200)
            }
        }
        
        // Atualizar UI se a MainActivity estiver visível
        FloatingOverlayService.instance?.atualizarStatus()
    }

    private fun clicarAceitar() {
        // Obter dimensões da tela
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels
        
        // A barra verde está aproximadamente no centro horizontal
        // e na parte inferior da tela (onde fica o botão aceitar)
        val x = screenWidth / 2f
        val y = screenHeight * 0.75f // Aproximadamente 75% da altura da tela
        
        Log.d(TAG, "Clicando em ACEITAR: x=$x, y=$y")
        realizarToque(x, y)
    }

    private fun clicarRejeitar() {
        // Obter dimensões da tela
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels
        
        // Botão de rejeitar está geralmente na parte inferior esquerda
        val x = screenWidth * 0.25f
        val y = screenHeight * 0.85f
        
        Log.d(TAG, "Clicando em REJEITAR: x=$x, y=$y")
        realizarToque(x, y)
    }

    private fun realizarToque(x: Float, y: Float) {
        val path = Path()
        path.moveTo(x, y)
        
        val gestureBuilder = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
        
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    // Classe de dados para informações da corrida
    data class InfoCorrida(
        val valorTotal: Double,
        val valorPorKm: Double,
        val distanciaBusca: Double,
        val distanciaCorrida: Double,
        val textoCompleto: String
    ) {
        override fun toString(): String {
            return "Corrida(valorTotal=$valorTotal, valorPorKm=$valorPorKm, distanciaBusca=$distanciaBusca, distanciaCorrida=$distanciaCorrida)"
        }
    }
}
