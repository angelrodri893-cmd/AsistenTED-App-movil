package com.asistented.app.interfaz

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.asistented.app.R
import com.asistented.app.datos.CatalogoTramites
import com.asistented.app.datos.modelos.ComentarioForo
import com.asistented.app.datos.modelos.PasoGuia
import com.asistented.app.datos.modelos.Tramite
import com.asistented.app.interfaz.tema.TemaAsistenTED
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun PantallaDetalleTramiteRedisenada(
    tramite: Tramite,
    pasosCompletados: Set<String>,
    comentarios: List<ComentarioForo>,
    usuarioActualId: String?,
    avatarId: String,
    puedeParticipar: Boolean,
    mostrarAvisoInicial: Boolean,
    estaLeyendo: Boolean,
    onRegresar: () -> Unit,
    onAbrirPerfil: () -> Unit,
    onAbrirPortal: () -> Unit,
    onAlternarLectura: () -> Unit,
    onAlternarPaso: (PasoGuia) -> Unit,
    onPublicarComentario: (String) -> Unit,
    onEditarComentario: (ComentarioForo, String) -> Unit,
    onEliminarComentario: (ComentarioForo) -> Unit,
    onResponderComentario: (ComentarioForo, String) -> Unit,
    onDescartarAviso: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primerPasoPendiente = tramite.steps.firstOrNull { it.id !in pasosCompletados }?.id
    var pasoExpandidoId by rememberSaveable(tramite.id) { mutableStateOf(primerPasoPendiente) }
    var requisitosExpandidos by rememberSaveable(tramite.id) { mutableStateOf(false) }
    var costoExpandido by rememberSaveable(tramite.id) { mutableStateOf(false) }
    val revisionesMarcadas = remember(tramite.id) { mutableStateMapOf<String, Set<Int>>() }

    LaunchedEffect(pasosCompletados) {
        if (pasoExpandidoId?.let { it in pasosCompletados } == true) {
            pasoExpandidoId = tramite.steps.firstOrNull { it.id !in pasosCompletados }?.id
        }
    }

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
                EncabezadoDetalle(
                    tramite = tramite,
                    avatarId = avatarId,
                    onRegresar = onRegresar,
                    onAbrirPerfil = onAbrirPerfil
                )
            }
            item { PresentacionTramite(tramite = tramite) }
            tramite.requisitosOficiales?.takeIf { it.isNotBlank() }?.let { requisitos ->
                item(key = "requisitos_${tramite.id}") {
                    InformacionOficialDesplegable(
                        titulo = stringResource(R.string.detail_official_requirements),
                        contenido = requisitos,
                        expandido = requisitosExpandidos,
                        fuente = tramite.fuenteOficial,
                        actualizadoEn = tramite.actualizadoEn,
                        onAlternar = { requisitosExpandidos = !requisitosExpandidos }
                    )
                }
            }
            tramite.costoOficial?.takeIf { it.isNotBlank() }?.let { costo ->
                item(key = "costo_${tramite.id}") {
                    val contenidoCosto = when {
                        costo.equals("Sí", ignoreCase = true) -> stringResource(R.string.detail_has_cost)
                        costo.equals("No", ignoreCase = true) -> stringResource(R.string.detail_no_cost)
                        else -> costo
                    }
                    InformacionOficialDesplegable(
                        titulo = stringResource(R.string.detail_official_cost),
                        contenido = contenidoCosto,
                        expandido = costoExpandido,
                        fuente = tramite.fuenteOficial,
                        actualizadoEn = tramite.actualizadoEn,
                        onAlternar = { costoExpandido = !costoExpandido }
                    )
                }
            }
            item { AvisoPortalOficial(institucion = tramite.institution) }
            item {
                AccionesDetalle(
                    estaLeyendo = estaLeyendo,
                    onAbrirPortal = onAbrirPortal,
                    onAlternarLectura = onAlternarLectura
                )
            }
            item {
                EncabezadoProcedimientoOficial(totalPasos = tramite.steps.size)
            }
            item {
                ProgresoDetalle(
                    completados = pasosCompletados.size.coerceAtMost(tramite.steps.size),
                    total = tramite.steps.size
                )
            }
            itemsIndexed(tramite.steps, key = { _, paso -> paso.id }) { indice, paso ->
                val completado = paso.id in pasosCompletados
                PasoDetalle(
                    paso = paso,
                    indice = indice,
                    completado = completado,
                    expandido = paso.id == pasoExpandidoId,
                    esUltimo = indice == tramite.steps.lastIndex,
                    revisionesMarcadas = if (completado) {
                        paso.elementosRevision.indices.toSet()
                    } else {
                        revisionesMarcadas[paso.id].orEmpty()
                    },
                    onAlternarExpandido = {
                        pasoExpandidoId = if (pasoExpandidoId == paso.id) null else paso.id
                    },
                    onAlternarCompletado = {
                        onAlternarPaso(paso)
                        if (completado) {
                            pasoExpandidoId = paso.id
                        }
                    },
                    onAlternarRevision = { indiceRevision ->
                        val actuales = revisionesMarcadas[paso.id].orEmpty()
                        val actualizados = if (indiceRevision in actuales) {
                            actuales - indiceRevision
                        } else {
                            actuales + indiceRevision
                        }
                        revisionesMarcadas[paso.id] = actualizados
                        // Al completar toda la lista, el progreso persistente avanza al siguiente paso.
                        if (!completado && actualizados.size == paso.elementosRevision.size) {
                            onAlternarPaso(paso)
                        }
                    }
                )
            }
            item {
                SeccionComentariosDetalle(
                    tramiteId = tramite.id,
                    comentarios = comentarios,
                    usuarioActualId = usuarioActualId,
                    puedeParticipar = puedeParticipar,
                    onPublicar = onPublicarComentario,
                    onEditar = onEditarComentario,
                    onEliminar = onEliminarComentario,
                    onResponder = onResponderComentario
                )
            }
        }

        if (mostrarAvisoInicial) {
            AvisoInicialDetalle(onDescartarAviso)
        }
    }
}

internal fun calcularProgresoDetalle(completados: Int, total: Int): Float =
    if (total <= 0) 0f else completados.coerceIn(0, total).toFloat() / total

internal fun construirTextoGuia(tramite: Tramite): String = buildString {
    append(tramite.title).append(". ")
    append(tramite.summary).append(". ")
    tramite.requisitosOficiales?.let { append("Requisitos oficiales. ").append(it).append(". ") }
    tramite.costoOficial?.let { append("Información de costo. ").append(it).append(". ") }
    tramite.steps.forEach { paso ->
        append(paso.title).append(". ")
        append(paso.description).append(". ")
        append(paso.textoAyuda).append(". ")
    }
}

@Composable
private fun EncabezadoDetalle(
    tramite: Tramite,
    avatarId: String,
    onRegresar: () -> Unit,
    onAbrirPerfil: () -> Unit
) {
    EncabezadoPantalla(
        titulo = stringResource(R.string.detail_guide_title, nombreCortoTramite(tramite.id)),
        descripcionRegresar = stringResource(R.string.detail_cd_back),
        avatarId = avatarId,
        onRegresar = onRegresar,
        onAbrirPerfil = onAbrirPerfil
    )
}

@Composable
private fun PresentacionTramite(tramite: Tramite) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .size(66.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconoTramiteDetalle(tramite.id),
                contentDescription = stringResource(R.string.detail_cd_procedure, tramite.title),
                modifier = Modifier.size(38.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(
            text = tramite.institution,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = tramite.summary,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InformacionOficialDesplegable(
    titulo: String,
    contenido: String,
    expandido: Boolean,
    fuente: String?,
    actualizadoEn: String?,
    onAlternar: () -> Unit
) {
    val descripcionAccion = stringResource(
        if (expandido) R.string.detail_collapse_official_section else R.string.detail_expand_official_section,
        titulo
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAlternar)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = titulo,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = descripcionAccion,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            AnimatedVisibility(visible = expandido) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = contenido,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!fuente.isNullOrBlank() || !actualizadoEn.isNullOrBlank()) {
                        Text(
                            text = listOfNotNull(
                                fuente,
                                actualizadoEn?.let { "Actualizado: $it" }
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoProcedimientoOficial(totalPasos: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.detail_official_procedure),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.detail_official_steps_count, totalPasos),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AvisoPortalOficial(institucion: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(R.string.detail_cd_information),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.detail_orientation, institucion),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AccionesDetalle(
    estaLeyendo: Boolean,
    onAbrirPortal: () -> Unit,
    onAlternarLectura: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onAbrirPortal,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(stringResource(R.string.detail_open_portal), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = stringResource(R.string.detail_cd_open_portal))
        }
        Button(
            onClick = onAlternarLectura,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Icon(
                imageVector = if (estaLeyendo) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = stringResource(
                    if (estaLeyendo) R.string.detail_cd_stop else R.string.detail_cd_listen
                )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(
                    if (estaLeyendo) R.string.detail_stop_guide else R.string.detail_listen_guide
                ),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ProgresoDetalle(completados: Int, total: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.detail_progress),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.detail_progress_count, completados, total),
                style = MaterialTheme.typography.labelLarge
            )
        }
        LinearProgressIndicator(
            progress = { calcularProgresoDetalle(completados, total) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun PasoDetalle(
    paso: PasoGuia,
    indice: Int,
    completado: Boolean,
    expandido: Boolean,
    esUltimo: Boolean,
    revisionesMarcadas: Set<Int>,
    onAlternarExpandido: () -> Unit,
    onAlternarCompletado: () -> Unit,
    onAlternarRevision: (Int) -> Unit
) {
    val descripcionAlternarPaso = stringResource(
        if (completado) R.string.detail_cd_step_complete else R.string.detail_cd_step_pending,
        indice + 1
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .width(DimensionesDiseno.objetivoTactil)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(DimensionesDiseno.objetivoTactil)
                    .clickable(onClick = onAlternarCompletado)
                    .semantics { contentDescription = descripcionAlternarPaso },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = if (completado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                    border = BorderStroke(2.dp, if (completado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (completado) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = (indice + 1).toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            if (!esUltimo) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(
                            if (completado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (esUltimo) 4.dp else 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.detail_step_title,
                    indice + 1,
                    paso.title.substringAfter(". ", paso.title)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAlternarExpandido),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textDecoration = if (completado) TextDecoration.LineThrough else TextDecoration.None,
                color = if (completado) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            if (expandido) {
                Text(
                    text = paso.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                TarjetaConsejo(texto = paso.textoAyuda)
                paso.elementosRevision.forEachIndexed { indiceRevision, revision ->
                    val descripcionRevision = stringResource(R.string.detail_cd_check_item, revision)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAlternarRevision(indiceRevision) }
                            .semantics { contentDescription = descripcionRevision },
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = indiceRevision in revisionesMarcadas,
                            onCheckedChange = { onAlternarRevision(indiceRevision) },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = revision,
                            modifier = Modifier.padding(top = 12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaConsejo(texto: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(DimensionesDiseno.paddingTarjeta),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = stringResource(R.string.detail_cd_advice),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.detail_advice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(texto, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SeccionComentariosDetalle(
    tramiteId: String,
    comentarios: List<ComentarioForo>,
    usuarioActualId: String?,
    puedeParticipar: Boolean,
    onPublicar: (String) -> Unit,
    onEditar: (ComentarioForo, String) -> Unit,
    onEliminar: (ComentarioForo) -> Unit,
    onResponder: (ComentarioForo, String) -> Unit
) {
    var textoComentario by rememberSaveable(tramiteId) { mutableStateOf("") }
    var intentoPublicar by rememberSaveable(tramiteId) { mutableStateOf(false) }
    var comentarioEditandoId by rememberSaveable(tramiteId) { mutableStateOf<String?>(null) }
    var textoEdicion by rememberSaveable(tramiteId) { mutableStateOf("") }
    var intentoEditar by rememberSaveable(tramiteId) { mutableStateOf(false) }
    var comentarioRespondiendoId by rememberSaveable(tramiteId) { mutableStateOf<String?>(null) }
    var textoRespuesta by rememberSaveable(tramiteId) { mutableStateOf("") }
    var intentoResponder by rememberSaveable(tramiteId) { mutableStateOf(false) }
    val comentariosPrincipales = comentarios.filter { it.respuestaAId == null }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalDivider()
        Text(
            text = stringResource(R.string.detail_questions),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (puedeParticipar) {
            CampoComentarioDetalle(
                valor = textoComentario,
                onValorChange = { textoComentario = it },
                etiqueta = stringResource(R.string.detail_comment_placeholder),
                mensajeError = stringResource(R.string.detail_error_comment),
                isError = intentoPublicar && textoComentario.isBlank(),
                minLineas = 4
            )
            Button(
                onClick = {
                    intentoPublicar = true
                    if (textoComentario.isNotBlank()) {
                        onPublicar(textoComentario.trim())
                        textoComentario = ""
                        intentoPublicar = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DimensionesDiseno.altoAccion),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(stringResource(R.string.detail_publish), fontWeight = FontWeight.SemiBold)
            }
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Text(
                    text = stringResource(R.string.detail_guest_forum),
                    modifier = Modifier.padding(DimensionesDiseno.paddingTarjeta),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (comentarios.isEmpty()) {
            Text(stringResource(R.string.detail_no_comments), style = MaterialTheme.typography.bodyMedium)
        } else {
            comentariosPrincipales.forEach { comentario ->
                TarjetaComentarioDetalle(
                    comentario = comentario,
                    esRespuesta = false,
                    esPropietario = puedeParticipar && usuarioActualId == comentario.userId,
                    puedeResponder = puedeParticipar && usuarioActualId != comentario.userId,
                    editando = comentarioEditandoId == comentario.id,
                    respondiendo = comentarioRespondiendoId == comentario.id,
                    textoEdicion = textoEdicion,
                    textoRespuesta = textoRespuesta,
                    errorEdicion = intentoEditar && textoEdicion.isBlank(),
                    errorRespuesta = intentoResponder && textoRespuesta.isBlank(),
                    onIniciarEdicion = {
                        comentarioEditandoId = comentario.id
                        textoEdicion = comentario.text
                        intentoEditar = false
                        comentarioRespondiendoId = null
                    },
                    onTextoEdicionChange = { textoEdicion = it },
                    onGuardarEdicion = {
                        intentoEditar = true
                        if (textoEdicion.isNotBlank()) {
                            onEditar(comentario, textoEdicion.trim())
                            comentarioEditandoId = null
                            textoEdicion = ""
                            intentoEditar = false
                        }
                    },
                    onCancelarEdicion = {
                        comentarioEditandoId = null
                        textoEdicion = ""
                        intentoEditar = false
                    },
                    onEliminar = { onEliminar(comentario) },
                    onIniciarRespuesta = {
                        comentarioRespondiendoId = comentario.id
                        textoRespuesta = ""
                        intentoResponder = false
                        comentarioEditandoId = null
                    },
                    onTextoRespuestaChange = { textoRespuesta = it },
                    onEnviarRespuesta = {
                        intentoResponder = true
                        if (textoRespuesta.isNotBlank()) {
                            onResponder(comentario, textoRespuesta.trim())
                            comentarioRespondiendoId = null
                            textoRespuesta = ""
                            intentoResponder = false
                        }
                    },
                    onCancelarRespuesta = {
                        comentarioRespondiendoId = null
                        textoRespuesta = ""
                        intentoResponder = false
                    }
                )
                comentarios.filter { it.respuestaAId == comentario.id }.forEach { respuesta ->
                    Box(modifier = Modifier.padding(start = 22.dp)) {
                        TarjetaComentarioDetalle(
                            comentario = respuesta,
                            esRespuesta = true,
                            esPropietario = puedeParticipar && usuarioActualId == respuesta.userId,
                            puedeResponder = false,
                            editando = comentarioEditandoId == respuesta.id,
                            respondiendo = false,
                            textoEdicion = textoEdicion,
                            textoRespuesta = "",
                            errorEdicion = intentoEditar && textoEdicion.isBlank(),
                            errorRespuesta = false,
                            onIniciarEdicion = {
                                comentarioEditandoId = respuesta.id
                                textoEdicion = respuesta.text
                                intentoEditar = false
                                comentarioRespondiendoId = null
                                textoRespuesta = ""
                                intentoResponder = false
                            },
                            onTextoEdicionChange = { textoEdicion = it },
                            onGuardarEdicion = {
                                intentoEditar = true
                                if (textoEdicion.isNotBlank()) {
                                    onEditar(respuesta, textoEdicion.trim())
                                    comentarioEditandoId = null
                                    textoEdicion = ""
                                    intentoEditar = false
                                }
                            },
                            onCancelarEdicion = {
                                comentarioEditandoId = null
                                textoEdicion = ""
                                intentoEditar = false
                            },
                            onEliminar = { onEliminar(respuesta) },
                            onIniciarRespuesta = {},
                            onTextoRespuestaChange = {},
                            onEnviarRespuesta = {},
                            onCancelarRespuesta = {}
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaComentarioDetalle(
    comentario: ComentarioForo,
    esRespuesta: Boolean,
    esPropietario: Boolean,
    puedeResponder: Boolean,
    editando: Boolean,
    respondiendo: Boolean,
    textoEdicion: String,
    textoRespuesta: String,
    errorEdicion: Boolean,
    errorRespuesta: Boolean,
    onIniciarEdicion: () -> Unit,
    onTextoEdicionChange: (String) -> Unit,
    onGuardarEdicion: () -> Unit,
    onCancelarEdicion: () -> Unit,
    onEliminar: () -> Unit,
    onIniciarRespuesta: () -> Unit,
    onTextoRespuestaChange: (String) -> Unit,
    onEnviarRespuesta: () -> Unit,
    onCancelarRespuesta: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (esRespuesta) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (esRespuesta) {
                    stringResource(R.string.detail_reply_from, comentario.username)
                } else {
                    comentario.username
                },
                fontWeight = FontWeight.Bold
            )
            Text(
                text = buildString {
                    append(formatearFechaComentario(comentario.createdAtMillis))
                    if (comentario.editadoEnMillis != null) {
                        append(" · ").append(stringResource(R.string.detail_edited))
                    }
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (editando) {
                CampoComentarioDetalle(
                    valor = textoEdicion,
                    onValorChange = onTextoEdicionChange,
                    etiqueta = stringResource(R.string.detail_edit_comment),
                    mensajeError = stringResource(R.string.detail_error_edit),
                    isError = errorEdicion
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onGuardarEdicion) { Text(stringResource(R.string.detail_save)) }
                    TextButton(onClick = onCancelarEdicion) { Text(stringResource(R.string.detail_cancel)) }
                }
            } else {
                Text(
                    text = comentario.text,
                    style = MaterialTheme.typography.bodyMedium
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (puedeResponder) {
                        AccionComentario(
                            texto = stringResource(R.string.detail_reply),
                            icono = Icons.AutoMirrored.Filled.Reply,
                            descripcion = stringResource(R.string.detail_cd_reply),
                            onClick = onIniciarRespuesta
                        )
                    }
                    if (esPropietario) {
                        AccionComentario(
                            texto = stringResource(R.string.detail_edit),
                            icono = Icons.Default.Edit,
                            descripcion = stringResource(R.string.detail_cd_edit),
                            onClick = onIniciarEdicion
                        )
                        AccionComentario(
                            texto = stringResource(R.string.detail_delete),
                            icono = Icons.Default.Delete,
                            descripcion = stringResource(R.string.detail_cd_delete),
                            onClick = onEliminar,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            if (respondiendo) {
                CampoComentarioDetalle(
                    valor = textoRespuesta,
                    onValorChange = onTextoRespuestaChange,
                    etiqueta = stringResource(R.string.detail_reply_to, comentario.username),
                    mensajeError = stringResource(R.string.detail_error_reply),
                    isError = errorRespuesta
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onEnviarRespuesta) { Text(stringResource(R.string.detail_send)) }
                    TextButton(onClick = onCancelarRespuesta) { Text(stringResource(R.string.detail_cancel)) }
                }
            }
        }
    }
}

@Composable
private fun AccionComentario(
    texto: String,
    icono: ImageVector,
    descripcion: String,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    TextButton(onClick = onClick, colors = ButtonDefaults.textButtonColors(contentColor = color)) {
        Icon(icono, contentDescription = descripcion, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text(texto)
    }
}

@Composable
private fun CampoComentarioDetalle(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    mensajeError: String,
    isError: Boolean,
    minLineas: Int = 2
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        shape = MaterialTheme.shapes.small,
        minLines = minLineas,
        maxLines = 6,
        isError = isError,
        supportingText = if (isError) ({ Text(mensajeError) }) else null,
        colors = coloresCampoDiseno()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvisoInicialDetalle(onDescartar: () -> Unit) {
    AvisoModalDiseno(
        mensaje = stringResource(R.string.detail_welcome_help),
        textoBoton = stringResource(R.string.home_do_not_remind),
        descripcionIcono = stringResource(R.string.detail_cd_welcome_help),
        onContinuar = onDescartar
    )
}

@StringRes
private fun nombreCortoRes(id: String): Int = when (id) {
    "cedula" -> R.string.detail_short_cedula
    "ruc" -> R.string.detail_short_ruc
    "impuestos" -> R.string.detail_short_taxes
    "iess" -> R.string.detail_short_iess
    "licencia" -> R.string.detail_short_license
    "pasaporte" -> R.string.detail_short_passport
    else -> R.string.detail_short_procedure
}

@Composable
private fun nombreCortoTramite(id: String): String = stringResource(nombreCortoRes(id))

private fun iconoTramiteDetalle(id: String): ImageVector = when (id) {
    "cedula" -> Icons.Default.Badge
    "ruc" -> Icons.Default.AccountBalance
    "licencia" -> Icons.Default.DirectionsCar
    "pasaporte" -> Icons.Default.Language
    else -> Icons.Default.Description
}

private fun formatearFechaComentario(millis: Long): String = Instant.ofEpochMilli(millis)
    .atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-EC")))

private val comentariosPreview = listOf(
    ComentarioForo(
        id = "comentario-1",
        tramiteId = "cedula",
        userId = "otro-usuario",
        username = "María",
        text = "¿El comprobante de pago debe estar impreso?",
        createdAtMillis = 1_754_500_000_000
    ),
    ComentarioForo(
        id = "respuesta-1",
        tramiteId = "cedula",
        userId = "usuario-preview",
        username = "Alexis",
        text = "Yo lo llevé impreso y también guardado en el teléfono.",
        createdAtMillis = 1_754_501_000_000,
        respuestaAId = "comentario-1"
    )
)

@Preview(name = "Detalle de tramite", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun PreviewDetalleTramite() {
    TemaAsistenTED(darkTheme = false) { DetallePreview() }
}

@Preview(name = "Detalle con ayuda", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun PreviewDetalleConAyuda() {
    TemaAsistenTED(darkTheme = false) { DetallePreview(mostrarAviso = true) }
}

@Preview(name = "Detalle amplio", showBackground = true, widthDp = 720, heightDp = 1000)
@Composable
private fun PreviewDetalleAmplio() {
    TemaAsistenTED(darkTheme = false) { DetallePreview() }
}

@Composable
private fun DetallePreview(mostrarAviso: Boolean = false) {
    PantallaDetalleTramiteRedisenada(
        tramite = CatalogoTramites.tramites.first(),
        pasosCompletados = setOf("preparar"),
        comentarios = comentariosPreview,
        usuarioActualId = "usuario-preview",
        avatarId = "avatar_1",
        puedeParticipar = true,
        mostrarAvisoInicial = mostrarAviso,
        estaLeyendo = false,
        onRegresar = {},
        onAbrirPerfil = {},
        onAbrirPortal = {},
        onAlternarLectura = {},
        onAlternarPaso = {},
        onPublicarComentario = {},
        onEditarComentario = { _, _ -> },
        onEliminarComentario = {},
        onResponderComentario = { _, _ -> },
        onDescartarAviso = {}
    )
}
