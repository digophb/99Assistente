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
import kotlin.math.abs
import kotlin.random.Random

/**
 * Accessibility Service para monitorar e automatizar o 99 Motorista
 */
class CorridaAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "99Assistente"
        private const val PACKAGE_99 = "com.taxis99"
        
        @Volatile
        var instance: CorridaAccessibilityService? = null
            private set
    }

    private val handler = Handler(Looper.getMainLooper())
    private var ultimaCorridaProcessada: String = ""
    private var tempoUltimaCorrida: Long = 0
    private var processandoCorrida = false

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
        val packageName = event.packageName?.toString()
        if (packageName != PACKAGE_99) return
        
        // Log para debug
        Log.d(TAG, "Evento recebido: type=${event.eventType}, package=$packageName, className=${event.className}")
        
        // Verificar tipo de evento
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
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
        if (processandoCorrida) {
            Log.d(TAG, "Já processando uma corrida, ignorando...")
            return
        }
        
        val rootNode = rootInActiveWindow ?: return
        
        try {
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
        val textos = mutableListOf<String>()
        buscarTextosRecursivo(rootNode, textos)
        
        Log.d(TAG, "Textos encontrados: $textos")
        
        var valorTotal: Double? = null
        var valorPorKm: Double? = null
        var distanciaBusca: Double? = null
        var distanciaCorrida: Double? = null
        
        for (texto in textos) {
            // Extrair valor por km
            val kmPattern = Regex("""R\$\s*([\d.,]+)\s*/\s*km""", RegexOption.IGNORE_CASE)
            kmPattern.find(texto)?.let {
                valorPorKm = parseValor(it.groupValues[1])
            }
            
            // Extrair valor total
            val valorPattern = Regex("""R\$\s*([\d.,]+)""")
            if (!texto.contains("/km", ignoreCase = true)) {
                valorPattern.find(texto)?.let {
                    val valor = parseValor(it.groupValues[1])
                    if (valor != null && valor > 0) {
                        if (valorTotal == null || valor > valorTotal) {
                            valorTotal = valor
                        }
                    }
                }
            }
            
            // Extrair distâncias
            val distPattern = Regex("""([\d.,]+)\s*km""", RegexOption.IGNORE_CASE)
            val distancias = distPattern.findAll(texto).mapNotNull { parseValor(it.groupValues[1]) }.toList()
            if (distancias.isNotEmpty()) {
                distanciaBusca = distancias.getOrNull(0) ?: 0.0
                distanciaCorrida = distancias.getOrNull(1) ?: distanciaCorrida
            }
        }
        
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
            val limpo = valorStr.replace(".", "").replace(",", ".").trim()
            limpo.toDouble()
        } catch (e: Exception) {
            null
        }
    }

    private fun isNovaCorrida(info: InfoCorrida): Boolean {
        val agora = System.currentTimeMillis()
        val identificador = "${info.valorPorKm}_${info.distanciaCorrida}"
        
        if (identificador == ultimaCorridaProcessada && (agora - tempoUltimaCorrida) < 5000) {
            return false
        }
        
        ultimaCorridaProcessada = identificador
        tempoUltimaCorrida = agora
        return true
    }

    private fun processarCorrida(info: InfoCorrida) {
        if (processandoCorrida) return
        processandoCorrida = true
        
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
                executarToquesAceitar()
            }
        } else {
            Log.d(TAG, "REJEITANDO corrida: $mensagem")
            ConfigManager.incrementarCorridasRejeitadas(applicationContext)
            // Não faz nada, deixa a corrida expirar
        }
        
        FloatingOverlayService.instance?.atualizarStatus()
    }

    private fun executarToquesAceitar() {
        // Verificar se os alvos estão configurados
        if (!ConfigManager.isAlvosConfigurados(applicationContext)) {
            Log.e(TAG, "Alvos não configurados!")
            processandoCorrida = false
            return
        }
        
        val delayMin = ConfigManager.getDelayInicioMin(applicationContext)
        val delayMax = ConfigManager.getDelayInicioMax(applicationContext)
        val duracaoToque = ConfigManager.getDuracaoToque(applicationContext)
        val intervaloMin = ConfigManager.getIntervaloMin(applicationContext)
        val intervaloMax = ConfigManager.getIntervaloMax(applicationContext)
        val mostrarIndicador = ConfigManager.isMostrarIndicador(applicationContext)
        
        // Sortear delay inicial
        val delayInicial = Random.nextInt(delayMin, delayMax + 1)
        Log.d(TAG, "Delay inicial: ${delayInicial}ms")
        
        handler.postDelayed({
            // Toque 1 - Alvo 1
            val x1 = ConfigManager.getAlvo1X(applicationContext).toFloat()
            val y1 = ConfigManager.getAlvo1Y(applicationContext).toFloat()
            
            if (mostrarIndicador) {
                TouchIndicatorOverlay.show(this, x1, y1, ConfigManager.getTamanhoAreaToque(applicationContext))
            }
            realizarToque(x1, y1, duracaoToque.toLong())
            
            // Sortear intervalo 1
            val intervalo1 = Random.nextInt(intervaloMin, intervaloMax + 1)
            Log.d(TAG, "Toque 1 em ($x1, $y1), intervalo: ${intervalo1}ms")
            
            handler.postDelayed({
                // Toque 2 - Alvo 2
                val x2 = ConfigManager.getAlvo2X(applicationContext).toFloat()
                val y2 = ConfigManager.getAlvo2Y(applicationContext).toFloat()
                
                if (mostrarIndicador) {
                    TouchIndicatorOverlay.show(this, x2, y2, ConfigManager.getTamanhoAreaToque(applicationContext))
                }
                realizarToque(x2, y2, duracaoToque.toLong())
                
                // Sortear intervalo 2
                val intervalo2 = Random.nextInt(intervaloMin, intervaloMax + 1)
                Log.d(TAG, "Toque 2 em ($x2, $y2), intervalo: ${intervalo2}ms")
                
                handler.postDelayed({
                    // Toque 3 - Alvo 3
                    val x3 = ConfigManager.getAlvo3X(applicationContext).toFloat()
                    val y3 = ConfigManager.getAlvo3Y(applicationContext).toFloat()
                    
                    if (mostrarIndicador) {
                        TouchIndicatorOverlay.show(this, x3, y3, ConfigManager.getTamanhoAreaToque(applicationContext))
                    }
                    realizarToque(x3, y3, duracaoToque.toLong())
                    
                    Log.d(TAG, "Toque 3 em ($x3, $y3) - Concluído!")
                    processandoCorrida = false
                }, intervalo2.toLong())
            }, intervalo1.toLong())
        }, delayInicial.toLong())
    }

    private fun realizarToque(x: Float, y: Float, duracao: Long) {
        val path = Path()
        path.moveTo(x, y)
        
        val gestureBuilder = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duracao))
        
        dispatchGesture(gestureBuilder.build(), null, null)
    }

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
