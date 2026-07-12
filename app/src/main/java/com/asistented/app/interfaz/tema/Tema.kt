package com.asistented.app.interfaz.tema

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.asistented.app.datos.modelos.ConfiguracionAccesibilidad

private val EsquemaOscuro = darkColorScheme(
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

private val EsquemaClaro = lightColorScheme(
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
fun TemaAsistenTED(
    darkTheme: Boolean = isSystemInDarkTheme(),
    configuracionAccesibilidad: ConfiguracionAccesibilidad = ConfiguracionAccesibilidad(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        configuracionAccesibilidad.altoContraste || darkTheme -> EsquemaOscuro
        else -> EsquemaClaro
    }
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val barrasClaras = !configuracionAccesibilidad.altoContraste && !darkTheme

            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = barrasClaras
                isAppearanceLightNavigationBars = barrasClaras
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Tipografia,
        content = content
    )
}
