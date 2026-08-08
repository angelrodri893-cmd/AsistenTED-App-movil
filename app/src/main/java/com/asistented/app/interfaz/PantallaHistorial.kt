package com.asistented.app.interfaz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.asistented.app.R
import com.asistented.app.datos.CatalogoTramites
import com.asistented.app.datos.modelos.Tramite
import com.asistented.app.interfaz.tema.TemaAsistenTED

@Composable
internal fun PantallaHistorialRedisenada(
    tramites: List<Tramite>,
    idsHistorial: List<String>,
    favoritos: Set<String>,
    avatarId: String,
    onRegresar: () -> Unit,
    onAbrirPerfil: () -> Unit,
    onAbrirTramite: (Tramite) -> Unit,
    onAlternarFavorito: (Tramite) -> Unit,
    modifier: Modifier = Modifier
) {
    val tramitesHistorial = remember(tramites, idsHistorial) {
        seleccionarTramitesHistorial(tramites, idsHistorial)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 280.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = DimensionesDiseno.anchoContenido)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = DimensionesDiseno.margenPantalla,
                top = 4.dp,
                end = DimensionesDiseno.margenPantalla,
                bottom = 24.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EncabezadoHistorial(
                    avatarId = avatarId,
                    onRegresar = onRegresar,
                    onAbrirPerfil = onAbrirPerfil
                )
            }
            if (tramitesHistorial.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { EstadoHistorialVacio() }
            } else {
                items(tramitesHistorial, key = { it.id }) { tramite ->
                    TarjetaHistorial(
                        tramite = tramite,
                        esFavorito = tramite.id in favoritos,
                        onAbrir = { onAbrirTramite(tramite) },
                        onAlternarFavorito = { onAlternarFavorito(tramite) }
                    )
                }
            }
        }
    }
}

// Los ids de Firebase definen el orden de consulta; el catalogo aporta el contenido actualizado.
internal fun seleccionarTramitesHistorial(
    tramites: List<Tramite>,
    idsHistorial: List<String>
): List<Tramite> {
    val tramitesPorId = tramites.associateBy { it.id }
    return idsHistorial.distinct().mapNotNull(tramitesPorId::get)
}

@Composable
private fun EncabezadoHistorial(
    avatarId: String,
    onRegresar: () -> Unit,
    onAbrirPerfil: () -> Unit
) {
    EncabezadoPantalla(
        titulo = stringResource(R.string.history_title),
        descripcionRegresar = stringResource(R.string.profile_cd_back),
        avatarId = avatarId,
        onRegresar = onRegresar,
        onAbrirPerfil = onAbrirPerfil
    )
}

@Composable
private fun TarjetaHistorial(
    tramite: Tramite,
    esFavorito: Boolean,
    onAbrir: () -> Unit,
    onAlternarFavorito: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(DimensionesDiseno.paddingTarjeta),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.img_plantilla_home),
                contentDescription = stringResource(R.string.home_cd_procedure_illustration, tramite.title),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2.36f)
                    .clip(MaterialTheme.shapes.extraSmall),
                contentScale = ContentScale.Crop
            )
            Text(
                text = tramite.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = tramite.institution,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = tramite.summary,
                style = MaterialTheme.typography.bodySmall,
                minLines = 3,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            AccionesTarjetaTramite(
                esFavorito = esFavorito,
                textoAccion = stringResource(R.string.home_view_guide),
                descripcionFavorito = stringResource(
                    if (esFavorito) R.string.home_cd_remove_favorite else R.string.home_cd_add_favorite
                ),
                onAbrir = onAbrir,
                onAlternarFavorito = onAlternarFavorito
            )
        }
    }
}

@Composable
private fun EstadoHistorialVacio() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = stringResource(R.string.history_cd_empty),
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.history_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.history_empty_description),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun iconoTramiteHistorial(id: String): ImageVector = when (id) {
    "cedula" -> Icons.Default.Badge
    "ruc" -> Icons.Default.AccountBalance
    "licencia" -> Icons.Default.DirectionsCar
    "pasaporte" -> Icons.Default.Language
    else -> Icons.Default.Description
}

@Preview(name = "Historial compacto", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewHistorialCompacto() {
    TemaAsistenTED(darkTheme = false) {
        HistorialPreview(ids = CatalogoTramites.tramites.take(3).map { it.id })
    }
}

@Preview(name = "Historial amplio", showBackground = true, widthDp = 720, heightDp = 900)
@Composable
private fun PreviewHistorialAmplio() {
    TemaAsistenTED(darkTheme = false) {
        HistorialPreview(ids = CatalogoTramites.tramites.map { it.id })
    }
}

@Preview(name = "Historial vacio", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewHistorialVacio() {
    TemaAsistenTED(darkTheme = false) { HistorialPreview(ids = emptyList()) }
}

@Composable
private fun HistorialPreview(ids: List<String>) {
    PantallaHistorialRedisenada(
        tramites = CatalogoTramites.tramites,
        idsHistorial = ids,
        favoritos = setOf("cedula"),
        avatarId = "avatar_1",
        onRegresar = {},
        onAbrirPerfil = {},
        onAbrirTramite = {},
        onAlternarFavorito = {}
    )
}
