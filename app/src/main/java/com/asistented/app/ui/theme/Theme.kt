package com.asistented.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.asistented.app.data.model.AccessibilitySettings

private val DarkColorScheme = darkColorScheme(
    primary = HighContrastYellow,
    secondary = EcuadorBlueSoft,
    tertiary = EcuadorRedSoft,
    background = HighContrastBackground,
    surface = HighContrastSurface,
    onPrimary = Ink,
    onSecondary = Ink,
    onTertiary = Ink,
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EcuadorBlue,
    secondary = EcuadorYellow,
    tertiary = EcuadorRed,
    background = Paper,
    surface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = SurfaceSoft,
    primaryContainer = EcuadorBlueSoft,
    secondaryContainer = EcuadorYellowSoft,
    tertiaryContainer = EcuadorRedSoft,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = Ink,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    onBackground = Ink,
    onSurface = Ink
)

@Composable
fun AsistenTEDv1Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accessibilitySettings: AccessibilitySettings = AccessibilitySettings(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        accessibilitySettings.highContrast || darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
