package com.asistented.app.data

import android.content.Context
import com.asistented.app.data.model.AccessibilitySettings

class LocalPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("asistented_preferences", Context.MODE_PRIVATE)

    fun loadAccessibility(): AccessibilitySettings = AccessibilitySettings(
        largeText = preferences.getBoolean(KEY_LARGE_TEXT, false),
        highContrast = preferences.getBoolean(KEY_HIGH_CONTRAST, false)
    )

    fun saveAccessibility(settings: AccessibilitySettings) {
        preferences.edit()
            .putBoolean(KEY_LARGE_TEXT, settings.largeText)
            .putBoolean(KEY_HIGH_CONTRAST, settings.highContrast)
            .apply()
    }

    companion object {
        private const val KEY_LARGE_TEXT = "large_text"
        private const val KEY_HIGH_CONTRAST = "high_contrast"
    }
}
