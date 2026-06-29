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

    companion object {
        private const val KEY_LARGE_TEXT = "large_text"
        private const val KEY_HIGH_CONTRAST = "high_contrast"
    }
}


