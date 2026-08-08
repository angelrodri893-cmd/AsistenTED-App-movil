package com.asistented.app.interfaz.tema

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import com.asistented.app.datos.modelos.ConfiguracionAccesibilidad

private const val ESCALA_TEXTO_GRANDE = 1.2f

private val EsquemaOscuro = darkColorScheme(
    primary = HighContrastYellow,
    onPrimary = Ink,
    primaryContainer = HighContrastContainer,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondary = HighContrastYellow,
    onSecondary = Ink,
    secondaryContainer = HighContrastYellowContainer,
    onSecondaryContainer = androidx.compose.ui.graphics.Color.White,
    tertiary = HighContrastRed,
    onTertiary = Ink,
    tertiaryContainer = HighContrastRedContainer,
    onTertiaryContainer = androidx.compose.ui.graphics.Color.White,
    background = HighContrastBackground,
    onBackground = androidx.compose.ui.graphics.Color.White,
    surface = HighContrastSurface,
    onSurface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = HighContrastContainer,
    onSurfaceVariant = androidx.compose.ui.graphics.Color.White,
    outline = HighContrastOutline,
    outlineVariant = HighContrastOutlineSoft,
    error = HighContrastRed,
    onError = Ink,
    errorContainer = HighContrastRedContainer,
    onErrorContainer = androidx.compose.ui.graphics.Color.White
)

private val EsquemaClaro = lightColorScheme(
    primary = EcuadorBlue,
    secondary = EcuadorYellow,
    tertiary = EcuadorRed,
    background = Paper,
    surface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = SurfaceSoft,
    primaryContainer = EcuadorBlueSoft,
    onPrimaryContainer = Ink,
    secondaryContainer = EcuadorYellowSoft,
    onSecondaryContainer = Ink,
    tertiaryContainer = EcuadorRedSoft,
    onTertiaryContainer = Ink,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = Ink,
    onTertiary = Ink,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = Ink.copy(alpha = 0.78f),
    outline = Outline,
    outlineVariant = OutlineSoft,
    error = Error,
    onError = androidx.compose.ui.graphics.Color.White,
    errorContainer = ErrorSoft,
    onErrorContainer = Ink
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
    val densidadSistema = LocalDensity.current
    val escalaAdicional = if (configuracionAccesibilidad.textoGrande) ESCALA_TEXTO_GRANDE else 1f
    val densidadAccesible = Density(
        density = densidadSistema.density,
        fontScale = densidadSistema.fontScale * escalaAdicional
    )

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

    // La escala se aplica desde la raiz para incluir textos propios y componentes Material.
    CompositionLocalProvider(LocalDensity provides densidadAccesible) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Tipografia,
            shapes = FormasAsistenTed,
            content = content
        )
    }
}
