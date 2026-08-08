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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.annotation.StringRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.asistented.app.datos.modelos.ConfiguracionAccesibilidad
import com.asistented.app.datos.modelos.Tramite
import com.asistented.app.interfaz.tema.TemaAsistenTED

private const val INSTITUCION_REGISTRO_CIVIL = "Registro Civil"
private const val INSTITUCION_SRI = "SRI"
private const val INSTITUCION_ANT = "ANT"

private data class FiltroInstitucion(
    val institucion: String?,
    @param:StringRes val etiquetaRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PantallaPrincipal(
    nombreUsuario: String,
    avatarId: String,
    tramites: List<Tramite>,
    favoritos: Set<String>,
    mostrarAvisoInicial: Boolean,
    onAbrirTramite: (Tramite) -> Unit,
    onAlternarFavorito: (Tramite) -> Unit,
    onAbrirPerfil: () -> Unit,
    onDescartarAviso: () -> Unit,
    modifier: Modifier = Modifier
) {
    var consulta by rememberSaveable { mutableStateOf("") }
    var institucionSeleccionada by rememberSaveable { mutableStateOf<String?>(null) }
    val tramitesFiltrados = remember(tramites, consulta, institucionSeleccionada) {
        filtrarTramites(tramites, consulta, institucionSeleccionada)
    }
    val busquedaSinResultados = consulta.isNotBlank() && tramitesFiltrados.isEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
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
            verticalArrangement = Arrangement.spacedBy(DimensionesDiseno.espacioPantalla)
        ) {
            item {
                EncabezadoPrincipal(
                    nombreUsuario = nombreUsuario,
                    avatarId = avatarId,
                    onAbrirPerfil = onAbrirPerfil
                )
            }
            item {
                CampoBusquedaPrincipal(
                    consulta = consulta,
                    onConsultaChange = { consulta = it },
                    isError = busquedaSinResultados
                )
            }
            item {
                FiltrosInstitucion(
                    institucionSeleccionada = institucionSeleccionada,
                    onSeleccionar = { institucionSeleccionada = it }
                )
            }
            if (tramitesFiltrados.isEmpty()) {
                item { EstadoSinResultados() }
            } else {
                items(tramitesFiltrados, key = { it.id }) { tramite ->
                    TarjetaTramitePrincipal(
                        tramite = tramite,
                        esFavorito = tramite.id in favoritos,
                        onAbrir = { onAbrirTramite(tramite) },
                        onAlternarFavorito = { onAlternarFavorito(tramite) }
                    )
                }
            }
        }

        if (mostrarAvisoInicial) {
            AvisoInicialPrincipal(onDescartarAviso = onDescartarAviso)
        }
    }
}

internal fun filtrarTramites(
    tramites: List<Tramite>,
    consulta: String,
    institucion: String?
): List<Tramite> {
    val textoBuscado = consulta.trim()
    return tramites.filter { tramite ->
        val coincideInstitucion = institucion == null || tramite.institution == institucion
        val coincideConsulta = textoBuscado.isBlank() ||
            tramite.title.contains(textoBuscado, ignoreCase = true) ||
            tramite.institution.contains(textoBuscado, ignoreCase = true)
        coincideInstitucion && coincideConsulta
    }
}

@Composable
private fun EncabezadoPrincipal(
    nombreUsuario: String,
    avatarId: String,
    onAbrirPerfil: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_greeting),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = nombreUsuario,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onAbrirPerfil,
                modifier = Modifier
                    .size(DimensionesDiseno.objetivoTactil)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                AvatarUsuario(
                    avatarId = avatarId,
                    modifier = Modifier.size(DimensionesDiseno.avatarEncabezado)
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun CampoBusquedaPrincipal(
    consulta: String,
    onConsultaChange: (String) -> Unit,
    isError: Boolean
) {
    OutlinedTextField(
        value = consulta,
        onValueChange = onConsultaChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        placeholder = { Text(stringResource(R.string.home_search_placeholder)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.home_cd_search)
            )
        },
        isError = isError,
        supportingText = if (isError) {
            { Text(stringResource(R.string.home_search_error)) }
        } else {
            null
        },
        colors = coloresCampoDiseno()
    )
}

@Composable
private fun FiltrosInstitucion(
    institucionSeleccionada: String?,
    onSeleccionar: (String?) -> Unit
) {
    val filtros = listOf(
        FiltroInstitucion(null, R.string.home_filter_all),
        FiltroInstitucion(INSTITUCION_REGISTRO_CIVIL, R.string.home_filter_registro_civil),
        FiltroInstitucion(INSTITUCION_SRI, R.string.home_filter_sri),
        FiltroInstitucion(INSTITUCION_ANT, R.string.home_filter_ant)
    )
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filtros, key = { it.institucion ?: "todos" }) { filtro ->
            val seleccionado = institucionSeleccionada == filtro.institucion
            FilterChip(
                selected = seleccionado,
                onClick = { onSeleccionar(filtro.institucion) },
                label = {
                    Text(
                        text = stringResource(filtro.etiquetaRes),
                        maxLines = 1
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        }
    }
}

@Composable
private fun TarjetaTramitePrincipal(
    tramite: Tramite,
    esFavorito: Boolean,
    onAbrir: () -> Unit,
    onAlternarFavorito: () -> Unit
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
            ImagenTramite(
                tramite = tramite,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2.36f)
                    .clip(MaterialTheme.shapes.extraSmall),
                contentScale = ContentScale.Crop
            )
            Text(
                text = tramite.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                lineHeight = MaterialTheme.typography.titleMedium.lineHeight,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = tramite.institution,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
private fun EstadoSinResultados() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = stringResource(R.string.home_cd_no_results),
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = stringResource(R.string.home_empty_results),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvisoInicialPrincipal(onDescartarAviso: () -> Unit) {
    AvisoModalDiseno(
        mensaje = stringResource(R.string.home_welcome_help),
        textoBoton = stringResource(R.string.home_do_not_remind),
        descripcionIcono = stringResource(R.string.home_cd_welcome_help),
        onContinuar = onDescartarAviso
    )
}

@Preview(name = "Principal compacta", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewPantallaPrincipalCompacta() {
    TemaAsistenTED(darkTheme = false) {
        PantallaPrincipalPreview()
    }
}

@Preview(name = "Principal con aviso", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewPantallaPrincipalConAviso() {
    TemaAsistenTED(darkTheme = false) {
        PantallaPrincipalPreview(mostrarAvisoInicial = true)
    }
}

@Preview(name = "Principal amplia", showBackground = true, widthDp = 720, heightDp = 1000)
@Composable
private fun PreviewPantallaPrincipalAmplia() {
    TemaAsistenTED(darkTheme = false) {
        PantallaPrincipalPreview()
    }
}

@Preview(name = "Principal 320 texto grande", showBackground = true, widthDp = 320, heightDp = 800)
@Composable
private fun PreviewPantallaPrincipalTextoGrande() {
    TemaAsistenTED(
        darkTheme = false,
        configuracionAccesibilidad = ConfiguracionAccesibilidad(textoGrande = true)
    ) {
        PantallaPrincipalPreview()
    }
}

@Composable
private fun PantallaPrincipalPreview(mostrarAvisoInicial: Boolean = false) {
    PantallaPrincipal(
        nombreUsuario = "Alexis Rodriguez",
        avatarId = "avatar_1",
        tramites = CatalogoTramites.tramites,
        favoritos = setOf("licencia"),
        mostrarAvisoInicial = mostrarAvisoInicial,
        onAbrirTramite = {},
        onAlternarFavorito = {},
        onAbrirPerfil = {},
        onDescartarAviso = {}
    )
}
