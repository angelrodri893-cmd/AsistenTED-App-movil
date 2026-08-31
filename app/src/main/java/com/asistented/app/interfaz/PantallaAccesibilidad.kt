package com.asistented.app.interfaz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asistented.app.presentacion.ControladorAsistenTed

@Composable
internal fun PantallaAccesibilidad(controlador: ControladorAsistenTed) {
    val configuracion = controlador.configuracionAccesibilidad
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Accesibilidad", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Ajusta la lectura visual de toda la app.", style = MaterialTheme.typography.bodyLarge)
        FilaConfiguracion(
            title = "Texto grande",
            description = "Aumenta el tamaño de explicaciones, ayudas y pasos.",
            checked = configuracion.textoGrande,
            onCheckedChange = { controlador.actualizarAccesibilidad(configuracion.copy(textoGrande = it)) }
        )
        FilaConfiguracion(
            title = "Alto contraste",
            description = "Usa fondo oscuro y colores más fuertes para leer mejor.",
            checked = configuracion.altoContraste,
            onCheckedChange = { controlador.actualizarAccesibilidad(configuracion.copy(altoContraste = it)) }
        )
    }
}

@Composable
private fun FilaConfiguracion(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
