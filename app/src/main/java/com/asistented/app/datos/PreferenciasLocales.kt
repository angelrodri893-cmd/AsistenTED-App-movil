package com.asistented.app.datos

import android.content.Context
import com.asistented.app.datos.modelos.ConfiguracionAccesibilidad

class PreferenciasLocales(context: Context) {
    private val preferences = context.getSharedPreferences("asistented_preferences", Context.MODE_PRIVATE)

    fun cargarAccesibilidad(): ConfiguracionAccesibilidad = ConfiguracionAccesibilidad(
        textoGrande = preferences.getBoolean(KEY_LARGE_TEXT, false),
        altoContraste = preferences.getBoolean(KEY_HIGH_CONTRAST, false)
    )

    fun guardarAccesibilidad(configuracion: ConfiguracionAccesibilidad) {
        preferences.edit()
            .putBoolean(KEY_LARGE_TEXT, configuracion.textoGrande)
            .putBoolean(KEY_HIGH_CONTRAST, configuracion.altoContraste)
            .apply()
    }

    fun cargarCacheTramitesGobEc(): String? =
        preferences.getString(KEY_GOBEC_CACHE_JSON, null)

    fun cargarFechaCacheTramitesGobEc(): Long =
        preferences.getLong(KEY_GOBEC_CACHE_TIME, 0L)

    fun guardarCacheTramitesGobEc(json: String, guardadoEnMillis: Long) {
        preferences.edit()
            .putString(KEY_GOBEC_CACHE_JSON, json)
            .putLong(KEY_GOBEC_CACHE_TIME, guardadoEnMillis)
            .apply()
    }

    fun marcarAyudaPrincipalPendiente(uid: String) {
        if (uid.isBlank()) return
        preferences.edit()
            .putBoolean(claveAyudaPrincipal(uid), true)
            .apply()
    }

    fun debeMostrarAyudaPrincipal(uid: String): Boolean =
        uid.isNotBlank() && preferences.getBoolean(claveAyudaPrincipal(uid), false)

    fun completarAyudaPrincipal(uid: String) {
        if (uid.isBlank()) return
        preferences.edit()
            .putBoolean(claveAyudaPrincipal(uid), false)
            .apply()
    }

    fun marcarAyudaFavoritosPendiente(uid: String) {
        if (uid.isBlank()) return
        preferences.edit()
            .putBoolean(claveAyudaFavoritos(uid), true)
            .apply()
    }

    fun debeMostrarAyudaFavoritos(uid: String): Boolean =
        uid.isNotBlank() && preferences.getBoolean(claveAyudaFavoritos(uid), false)

    fun completarAyudaFavoritos(uid: String) {
        if (uid.isBlank()) return
        preferences.edit()
            .putBoolean(claveAyudaFavoritos(uid), false)
            .apply()
    }

    fun marcarAyudaNotificacionesPendiente(uid: String) {
        if (uid.isBlank()) return
        preferences.edit()
            .putBoolean(claveAyudaNotificaciones(uid), true)
            .apply()
    }

    fun debeMostrarAyudaNotificaciones(uid: String): Boolean =
        uid.isNotBlank() && preferences.getBoolean(claveAyudaNotificaciones(uid), false)

    fun completarAyudaNotificaciones(uid: String) {
        if (uid.isBlank()) return
        preferences.edit()
            .putBoolean(claveAyudaNotificaciones(uid), false)
            .apply()
    }

    fun marcarAyudaPerfilPendiente(uid: String) {
        if (uid.isBlank()) return
        preferences.edit()
            .putBoolean(claveAyudaPerfil(uid), true)
            .apply()
    }

    fun debeMostrarAyudaPerfil(uid: String): Boolean =
        uid.isNotBlank() && preferences.getBoolean(claveAyudaPerfil(uid), false)

    fun completarAyudaPerfil(uid: String) {
        if (uid.isBlank()) return
        preferences.edit()
            .putBoolean(claveAyudaPerfil(uid), false)
            .apply()
    }

    fun marcarAyudaDetallePendiente(uid: String) {
        if (uid.isBlank()) return
        preferences.edit()
            .putBoolean(claveAyudaDetalle(uid), true)
            .apply()
    }

    fun debeMostrarAyudaDetalle(uid: String): Boolean =
        uid.isNotBlank() && preferences.getBoolean(claveAyudaDetalle(uid), false)

    fun completarAyudaDetalle(uid: String) {
        if (uid.isBlank()) return
        preferences.edit()
            .putBoolean(claveAyudaDetalle(uid), false)
            .apply()
    }

    // El uid evita que la decision de una cuenta oculte la ayuda para otra cuenta del dispositivo.
    private fun claveAyudaPrincipal(uid: String) = "$KEY_HOME_HELP_PREFIX$uid"
    private fun claveAyudaFavoritos(uid: String) = "$KEY_FAVORITES_HELP_PREFIX$uid"
    private fun claveAyudaNotificaciones(uid: String) = "$KEY_NOTIFICATIONS_HELP_PREFIX$uid"
    private fun claveAyudaPerfil(uid: String) = "$KEY_PROFILE_HELP_PREFIX$uid"
    private fun claveAyudaDetalle(uid: String) = "$KEY_DETAIL_HELP_PREFIX$uid"

    companion object {
        private const val KEY_LARGE_TEXT = "large_text"
        private const val KEY_HIGH_CONTRAST = "high_contrast"
        private const val KEY_HOME_HELP_PREFIX = "home_help_pending_"
        private const val KEY_FAVORITES_HELP_PREFIX = "favorites_help_pending_"
        private const val KEY_NOTIFICATIONS_HELP_PREFIX = "notifications_help_pending_"
        private const val KEY_PROFILE_HELP_PREFIX = "profile_help_pending_"
        private const val KEY_DETAIL_HELP_PREFIX = "detail_help_pending_"
        private const val KEY_GOBEC_CACHE_JSON = "gobec_tramites_cache_json"
        private const val KEY_GOBEC_CACHE_TIME = "gobec_tramites_cache_time"
    }
}


