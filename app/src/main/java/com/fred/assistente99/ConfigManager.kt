package com.fred.assistente99

import android.content.Context
import android.content.SharedPreferences

/**
 * Gerenciador de configurações do assistente
 */
object ConfigManager {
    
    private const val PREFS_NAME = "assistente99_prefs"
    
    // Configurações de corrida
    private const val KEY_VALOR_MINIMO_KM = "valor_minimo_km"
    private const val KEY_DISTANCIA_MAXIMA_BUSCA = "distancia_maxima_busca"
    private const val KEY_AUTO_ACEITAR = "auto_aceitar"
    private const val KEY_SERVICO_ATIVO = "servico_ativo"
    private const val KEY_CORRIDAS_ACEITAS = "corridas_aceitas"
    private const val KEY_CORRIDAS_REJEITADAS = "corridas_rejeitadas"
    private const val KEY_ULTIMA_CORRIDA = "ultima_corrida"
    
    // Configurações de toque
    private const val KEY_QUANTIDADE_TOQUES = "quantidade_toques"
    private const val KEY_DURACAO_TOQUE = "duracao_toque"
    private const val KEY_INTERVALO_MIN = "intervalo_min"
    private const val KEY_INTERVALO_MAX = "intervalo_max"
    private const val KEY_DELAY_INICIO_MIN = "delay_inicio_min"
    private const val KEY_DELAY_INICIO_MAX = "delay_inicio_max"
    private const val KEY_TAMANHO_AREA_TOQUE = "tamanho_area_toque"
    private const val KEY_MOSTRAR_INDICADOR = "mostrar_indicador"
    
    // Posições dos 3 alvos
    private const val KEY_ALVO1_X = "alvo1_x"
    private const val KEY_ALVO1_Y = "alvo1_y"
    private const val KEY_ALVO2_X = "alvo2_x"
    private const val KEY_ALVO2_Y = "alvo2_y"
    private const val KEY_ALVO3_X = "alvo3_x"
    private const val KEY_ALVO3_Y = "alvo3_y"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // === CONFIGURAÇÕES DE CORRIDA ===
    
    fun getValorMinimoKm(context: Context): Double {
        return getPrefs(context).getFloat(KEY_VALOR_MINIMO_KM, 2.0f).toDouble()
    }
    
    fun setValorMinimoKm(context: Context, valor: Double) {
        getPrefs(context).edit().putFloat(KEY_VALOR_MINIMO_KM, valor.toFloat()).apply()
    }
    
    fun getDistanciaMaximaBusca(context: Context): Double {
        return getPrefs(context).getFloat(KEY_DISTANCIA_MAXIMA_BUSCA, 10.0f).toDouble()
    }
    
    fun setDistanciaMaximaBusca(context: Context, valor: Double) {
        getPrefs(context).edit().putFloat(KEY_DISTANCIA_MAXIMA_BUSCA, valor.toFloat()).apply()
    }
    
    fun isAutoAceitar(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_ACEITAR, true)
    }
    
    fun setAutoAceitar(context: Context, ativo: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_ACEITAR, ativo).apply()
    }
    
    fun isServicoAtivo(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SERVICO_ATIVO, false)
    }
    
    fun setServicoAtivo(context: Context, ativo: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SERVICO_ATIVO, ativo).apply()
    }
    
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
    
    fun getUltimaCorrida(context: Context): String {
        return getPrefs(context).getString(KEY_ULTIMA_CORRIDA, "Nenhuma ainda") ?: "Nenhuma ainda"
    }
    
    fun setUltimaCorrida(context: Context, info: String) {
        getPrefs(context).edit().putString(KEY_ULTIMA_CORRIDA, info).apply()
    }
    
    // === CONFIGURAÇÕES DE TOQUE ===
    
    fun getQuantidadeToques(context: Context): Int {
        return 3 // Fixo em 3 toques
    }
    
    fun getDuracaoToque(context: Context): Int {
        return getPrefs(context).getInt(KEY_DURACAO_TOQUE, 150)
    }
    
    fun setDuracaoToque(context: Context, duracao: Int) {
        getPrefs(context).edit().putInt(KEY_DURACAO_TOQUE, duracao).apply()
    }
    
    fun getIntervaloMin(context: Context): Int {
        return getPrefs(context).getInt(KEY_INTERVALO_MIN, 100)
    }
    
    fun setIntervaloMin(context: Context, valor: Int) {
        getPrefs(context).edit().putInt(KEY_INTERVALO_MIN, valor).apply()
    }
    
    fun getIntervaloMax(context: Context): Int {
        return getPrefs(context).getInt(KEY_INTERVALO_MAX, 200)
    }
    
    fun setIntervaloMax(context: Context, valor: Int) {
        getPrefs(context).edit().putInt(KEY_INTERVALO_MAX, valor).apply()
    }
    
    fun getDelayInicioMin(context: Context): Int {
        return getPrefs(context).getInt(KEY_DELAY_INICIO_MIN, 100)
    }
    
    fun setDelayInicioMin(context: Context, valor: Int) {
        getPrefs(context).edit().putInt(KEY_DELAY_INICIO_MIN, valor).apply()
    }
    
    fun getDelayInicioMax(context: Context): Int {
        return getPrefs(context).getInt(KEY_DELAY_INICIO_MAX, 300)
    }
    
    fun setDelayInicioMax(context: Context, valor: Int) {
        getPrefs(context).edit().putInt(KEY_DELAY_INICIO_MAX, valor).apply()
    }
    
    fun getTamanhoAreaToque(context: Context): Int {
        return getPrefs(context).getInt(KEY_TAMANHO_AREA_TOQUE, 100)
    }
    
    fun setTamanhoAreaToque(context: Context, tamanho: Int) {
        getPrefs(context).edit().putInt(KEY_TAMANHO_AREA_TOQUE, tamanho).apply()
    }
    
    fun isMostrarIndicador(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_MOSTRAR_INDICADOR, true)
    }
    
    fun setMostrarIndicador(context: Context, mostrar: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_MOSTRAR_INDICADOR, mostrar).apply()
    }
    
    // === POSIÇÕES DOS 3 ALVOS ===
    
    fun getAlvo1X(context: Context): Int {
        return getPrefs(context).getInt(KEY_ALVO1_X, -1)
    }
    
    fun getAlvo1Y(context: Context): Int {
        return getPrefs(context).getInt(KEY_ALVO1_Y, -1)
    }
    
    fun setAlvo1(context: Context, x: Int, y: Int) {
        getPrefs(context).edit()
            .putInt(KEY_ALVO1_X, x)
            .putInt(KEY_ALVO1_Y, y)
            .apply()
    }
    
    fun getAlvo2X(context: Context): Int {
        return getPrefs(context).getInt(KEY_ALVO2_X, -1)
    }
    
    fun getAlvo2Y(context: Context): Int {
        return getPrefs(context).getInt(KEY_ALVO2_Y, -1)
    }
    
    fun setAlvo2(context: Context, x: Int, y: Int) {
        getPrefs(context).edit()
            .putInt(KEY_ALVO2_X, x)
            .putInt(KEY_ALVO2_Y, y)
            .apply()
    }
    
    fun getAlvo3X(context: Context): Int {
        return getPrefs(context).getInt(KEY_ALVO3_X, -1)
    }
    
    fun getAlvo3Y(context: Context): Int {
        return getPrefs(context).getInt(KEY_ALVO3_Y, -1)
    }
    
    fun setAlvo3(context: Context, x: Int, y: Int) {
        getPrefs(context).edit()
            .putInt(KEY_ALVO3_X, x)
            .putInt(KEY_ALVO3_Y, y)
            .apply()
    }
    
    fun isAlvosConfigurados(context: Context): Boolean {
        return getAlvo1X(context) > 0 && getAlvo2X(context) > 0 && getAlvo3X(context) > 0
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
