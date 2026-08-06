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
    mostrarAvisoInicial: Boolean,
    textoGrande: Boolean,
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
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, top = 2.dp, end = 14.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EncabezadoFavoritos(
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
                        textoGrande = textoGrande,
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
    onRegresar: () -> Unit,
    onAbrirPerfil: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onRegresar, modifier = Modifier.size(40.dp)) {
                Image(
                    painter = painterResource(R.drawable.ic_favoritos_regresar),
                    contentDescription = stringResource(R.string.favorites_cd_back),
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
            }
            Text(
                text = stringResource(R.string.favorites_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = onAbrirPerfil,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_home_usuario),
                    contentDescription = stringResource(R.string.home_cd_open_profile),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun TarjetaFavorito(
    tramite: Tramite,
    textoGrande: Boolean,
    onAbrir: () -> Unit,
    onEliminarFavorito: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(7.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.img_plantilla_home),
                contentDescription = stringResource(
                    R.string.home_cd_procedure_illustration,
                    tramite.title
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.65f)
                    .clip(RoundedCornerShape(5.dp)),
                contentScale = ContentScale.Crop
            )
            Text(
                text = tramite.title,
                style = if (textoGrande) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.bodySmall
                },
                fontWeight = FontWeight.Medium,
                minLines = 3,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = tramite.institution,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onAbrir,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_view_guide),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }
                OutlinedIconButton(
                    onClick = onEliminarFavorito,
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = stringResource(R.string.home_cd_remove_favorite),
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
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
    val estadoHoja = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )
    ModalBottomSheet(
        onDismissRequest = {},
        sheetState = estadoHoja,
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
        tonalElevation = 0.dp,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 28.dp, top = 26.dp, end = 28.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SentimentSatisfiedAlt,
                contentDescription = stringResource(R.string.favorites_cd_welcome_help),
                modifier = Modifier.size(58.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.favorites_welcome_help),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onDescartarAviso,
                modifier = Modifier
                    .widthIn(max = 270.dp)
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(R.string.home_do_not_remind),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
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
        mostrarAvisoInicial = mostrarAvisoInicial,
        textoGrande = false,
        onRegresar = {},
        onAbrirPerfil = {},
        onAbrirTramite = {},
        onAlternarFavorito = {},
        onDescartarAviso = {}
    )
}
