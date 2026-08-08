package com.asistented.app.interfaz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PantallaFavoritos(
    tramites: List<Tramite>,
    favoritos: Set<String>,
    avatarId: String,
    mostrarAvisoInicial: Boolean,
    onRegresar: () -> Unit,
    onAbrirPerfil: () -> Unit,
    onAbrirTramite: (Tramite) -> Unit,
    onAlternarFavorito: (Tramite) -> Unit,
    onDescartarAviso: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tramitesFavoritos = remember(tramites, favoritos) {
        seleccionarTramitesFavoritos(tramites, favoritos)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Adaptive conserva dos columnas en telefonos y aprovecha el ancho adicional en tablets.
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 148.dp),
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
                EncabezadoFavoritos(
                    avatarId = avatarId,
                    onRegresar = onRegresar,
                    onAbrirPerfil = onAbrirPerfil
                )
            }

            if (tramitesFavoritos.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EstadoFavoritosVacio()
                }
            } else {
                items(tramitesFavoritos, key = { it.id }) { tramite ->
                    TarjetaFavorito(
                        tramite = tramite,
                        onAbrir = { onAbrirTramite(tramite) },
                        onEliminarFavorito = { onAlternarFavorito(tramite) }
                    )
                }
            }
        }

        if (mostrarAvisoInicial) {
            AvisoInicialFavoritos(onDescartarAviso = onDescartarAviso)
        }
    }
}

// El orden del catalogo se mantiene estable aunque los ids favoritos provengan de Firebase.
internal fun seleccionarTramitesFavoritos(
    tramites: List<Tramite>,
    favoritos: Set<String>
): List<Tramite> = tramites
    .distinctBy { it.id }
    .filter { it.id in favoritos }

@Composable
private fun EncabezadoFavoritos(
    avatarId: String,
    onRegresar: () -> Unit,
    onAbrirPerfil: () -> Unit
) {
    EncabezadoPantalla(
        titulo = stringResource(R.string.favorites_title),
        descripcionRegresar = stringResource(R.string.favorites_cd_back),
        avatarId = avatarId,
        onRegresar = onRegresar,
        onAbrirPerfil = onAbrirPerfil
    )
}

@Composable
private fun TarjetaFavorito(
    tramite: Tramite,
    onAbrir: () -> Unit,
    onEliminarFavorito: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(DimensionesDiseno.paddingTarjeta),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.img_plantilla_home),
                contentDescription = stringResource(
                    R.string.home_cd_procedure_illustration,
                    tramite.title
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2.36f)
                    .clip(MaterialTheme.shapes.extraSmall),
                contentScale = ContentScale.Crop
            )
            Text(
                text = tramite.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = tramite.institution,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            AccionesTarjetaTramite(
                esFavorito = true,
                textoAccion = stringResource(R.string.home_view_guide),
                descripcionFavorito = stringResource(R.string.home_cd_remove_favorite),
                onAbrir = onAbrir,
                onAlternarFavorito = onEliminarFavorito
            )
        }
    }
}

@Composable
private fun EstadoFavoritosVacio() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.BookmarkBorder,
            contentDescription = stringResource(R.string.favorites_cd_empty),
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.favorites_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.favorites_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvisoInicialFavoritos(onDescartarAviso: () -> Unit) {
    AvisoModalDiseno(
        mensaje = stringResource(R.string.favorites_welcome_help),
        textoBoton = stringResource(R.string.home_do_not_remind),
        descripcionIcono = stringResource(R.string.favorites_cd_welcome_help),
        onContinuar = onDescartarAviso
    )
}

@Preview(name = "Favoritos compacta", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewPantallaFavoritosCompacta() {
    TemaAsistenTED(darkTheme = false) {
        PantallaFavoritosPreview()
    }
}

@Preview(name = "Favoritos con aviso", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewPantallaFavoritosConAviso() {
    TemaAsistenTED(darkTheme = false) {
        PantallaFavoritosPreview(mostrarAvisoInicial = true)
    }
}

@Preview(name = "Favoritos amplia", showBackground = true, widthDp = 720, heightDp = 1000)
@Composable
private fun PreviewPantallaFavoritosAmplia() {
    TemaAsistenTED(darkTheme = false) {
        PantallaFavoritosPreview()
    }
}

@Composable
private fun PantallaFavoritosPreview(mostrarAvisoInicial: Boolean = false) {
    val tramites = CatalogoTramites.tramites
    PantallaFavoritos(
        tramites = tramites,
        favoritos = tramites.map { it.id }.toSet(),
        avatarId = "avatar_1",
        mostrarAvisoInicial = mostrarAvisoInicial,
        onRegresar = {},
        onAbrirPerfil = {},
        onAbrirTramite = {},
        onAlternarFavorito = {},
        onDescartarAviso = {}
    )
}
