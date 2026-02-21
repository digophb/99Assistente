package com.fred.assistente99

import android.content.Context
import android.content.SharedPreferences

/**
 * Gerenciador de configurações do assistente
 */
object ConfigManager {
    
    private const val PREFS_NAME = "assistente99_prefs"
    
    private const val KEY_VALOR_MINIMO_KM = "valor_minimo_km"
    private const val KEY_DISTANCIA_MAXIMA_BUSCA = "distancia_maxima_busca"
    private const val KEY_AUTO_ACEITAR = "auto_aceitar"
    private const val KEY_SERVICO_ATIVO = "servico_ativo"
    private const val KEY_CORRIDAS_ACEITAS = "corridas_aceitas"
    private const val KEY_CORRIDAS_REJEITADAS = "corridas_rejeitadas"
    private const val KEY_ULTIMA_CORRIDA = "ultima_corrida"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Valor mínimo por km
    fun getValorMinimoKm(context: Context): Double {
        return getPrefs(context).getFloat(KEY_VALOR_MINIMO_KM, 2.0f).toDouble()
    }
    
    fun setValorMinimoKm(context: Context, valor: Double) {
        getPrefs(context).edit().putFloat(KEY_VALOR_MINIMO_KM, valor.toFloat()).apply()
    }
    
    // Distância máxima para buscar passageiro
    fun getDistanciaMaximaBusca(context: Context): Double {
        return getPrefs(context).getFloat(KEY_DISTANCIA_MAXIMA_BUSCA, 10.0f).toDouble()
    }
    
    fun setDistanciaMaximaBusca(context: Context, valor: Double) {
        getPrefs(context).edit().putFloat(KEY_DISTANCIA_MAXIMA_BUSCA, valor.toFloat()).apply()
    }
    
    // Auto aceitar ativado
    fun isAutoAceitar(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_ACEITAR, true)
    }
    
    fun setAutoAceitar(context: Context, ativo: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_ACEITAR, ativo).apply()
    }
    
    // Serviço ativo
    fun isServicoAtivo(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SERVICO_ATIVO, false)
    }
    
    fun setServicoAtivo(context: Context, ativo: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SERVICO_ATIVO, ativo).apply()
    }
    
    // Contadores
    fun getCorridasAceitas(context: Context): Int {
        return getPrefs(context).getInt(KEY_CORRIDAS_ACEITAS, 0)
    }
    
    fun incrementarCorridasAceitas(context: Context) {
        val atual = getCorridasAceitas(context)
        getPrefs(context).edit().putInt(KEY_CORRIDAS_ACEITAS, atual + 1).apply()
    }
    
    fun getCorridasRejeitadas(context: Context): Int {
        return getPrefs(context).getInt(KEY_CORRIDAS_REJEITADAS, 0)
    }
    
    fun incrementarCorridasRejeitadas(context: Context) {
        val atual = getCorridasRejeitadas(context)
        getPrefs(context).edit().putInt(KEY_CORRIDAS_REJEITADAS, atual + 1).apply()
    }
    
    // Última corrida
    fun getUltimaCorrida(context: Context): String {
        return getPrefs(context).getString(KEY_ULTIMA_CORRIDA, "Nenhuma ainda") ?: "Nenhuma ainda"
    }
    
    fun setUltimaCorrida(context: Context, info: String) {
        getPrefs(context).edit().putString(KEY_ULTIMA_CORRIDA, info).apply()
    }
    
    // Reset estatísticas
    fun resetarEstatisticas(context: Context) {
        getPrefs(context).edit()
            .putInt(KEY_CORRIDAS_ACEITAS, 0)
            .putInt(KEY_CORRIDAS_REJEITADAS, 0)
            .putString(KEY_ULTIMA_CORRIDA, "Nenhuma ainda")
            .apply()
    }
}
