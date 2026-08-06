package com.asistented.app.interfaz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.asistented.app.R
import com.asistented.app.datos.CatalogoTramites
import com.asistented.app.datos.modelos.Recordatorio
import com.asistented.app.datos.modelos.Tramite
import com.asistented.app.interfaz.tema.TemaAsistenTED
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class SelectorFechaHora {
    Ninguno,
    Fecha,
    Hora
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PantallaNotificaciones(
    tramites: List<Tramite>,
    recordatorios: List<Recordatorio>,
    esInvitado: Boolean,
    mostrarAvisoInicial: Boolean,
    onRegresar: () -> Unit,
    onAbrirPerfil: () -> Unit,
    onGuardarRecordatorio: (String, String, String, Long) -> Unit,
    onBorrarRecordatorio: (Recordatorio) -> Unit,
    onDescartarAviso: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textoBusqueda by rememberSaveable { mutableStateOf("") }
    var tramiteSeleccionadoId by rememberSaveable { mutableStateOf<String?>(null) }
    var titulo by rememberSaveable { mutableStateOf("") }
    var nota by rememberSaveable { mutableStateOf("") }
    var diaSeleccionado by rememberSaveable { mutableStateOf<Long?>(null) }
    var horaSeleccionada by rememberSaveable { mutableStateOf<Int?>(null) }
    var minutoSeleccionado by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectorActivo by rememberSaveable { mutableStateOf(SelectorFechaHora.Ninguno) }
    var mostrarErrores by rememberSaveable { mutableStateOf(false) }

    val fechaSeleccionada = diaSeleccionado?.let(LocalDate::ofEpochDay)
    val programadoEnMillis = remember(diaSeleccionado, horaSeleccionada, minutoSeleccionado) {
        if (fechaSeleccionada != null && horaSeleccionada != null && minutoSeleccionado != null) {
            combinarFechaHora(
                fecha = fechaSeleccionada!!,
                hora = horaSeleccionada!!,
                minuto = minutoSeleccionado!!,
                zona = ZoneId.systemDefault()
            )
        } else {
            null
        }
    }
    val fechaHoraInvalida = programadoEnMillis != null && programadoEnMillis <= System.currentTimeMillis()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, top = 2.dp, end = 14.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                EncabezadoNotificaciones(
                    onRegresar = onRegresar,
                    onAbrirPerfil = onAbrirPerfil
                )
            }

            if (esInvitado) {
                item { AvisoCuentaNecesaria() }
            } else {
                item {
                    FormularioNotificacion(
                        tramites = tramites,
                        textoBusqueda = textoBusqueda,
                        tramiteSeleccionadoId = tramiteSeleccionadoId,
                        titulo = titulo,
                        nota = nota,
                        fechaSeleccionada = fechaSeleccionada,
                        horaSeleccionada = horaSeleccionada,
                        minutoSeleccionado = minutoSeleccionado,
                        selectorActivo = selectorActivo,
                        mostrarErrores = mostrarErrores,
                        fechaHoraInvalida = fechaHoraInvalida,
                        onTextoBusquedaChange = {
                            textoBusqueda = it
                            tramiteSeleccionadoId = null
                        },
                        onSeleccionarTramite = { tramite ->
                            tramiteSeleccionadoId = tramite.id
                            textoBusqueda = tramite.title
                        },
                        onLimpiarTramite = {
                            tramiteSeleccionadoId = null
                            textoBusqueda = ""
                        },
                        onTituloChange = { titulo = it },
                        onNotaChange = { nota = it },
                        onAbrirFecha = { selectorActivo = SelectorFechaHora.Fecha },
                        onCancelarSelector = { selectorActivo = SelectorFechaHora.Ninguno },
                        onConfirmarFecha = { fecha ->
                            // Se guarda el dia como Long para que rememberSaveable pueda restaurarlo sin Saver personalizado.
                            diaSeleccionado = fecha.toEpochDay()
                            selectorActivo = SelectorFechaHora.Hora
                        },
                        onConfirmarHora = { hora, minuto ->
                            horaSeleccionada = hora
                            minutoSeleccionado = minuto
                            selectorActivo = SelectorFechaHora.Ninguno
                        },
                        onGuardar = {
                            mostrarErrores = true
                            val tramiteId = tramiteSeleccionadoId
                            val fechaProgramada = programadoEnMillis
                            if (tramiteId != null && titulo.isNotBlank() && fechaProgramada != null && !fechaHoraInvalida) {
                                onGuardarRecordatorio(
                                    tramiteId,
                                    titulo.trim(),
                                    nota.trim(),
                                    fechaProgramada
                                )
                                // Se limpia el formulario solo cuando todos los datos fueron validados.
                                textoBusqueda = ""
                                tramiteSeleccionadoId = null
                                titulo = ""
                                nota = ""
                                diaSeleccionado = null
                                horaSeleccionada = null
                                minutoSeleccionado = null
                                mostrarErrores = false
                            }
                        }
                    )
                }
            }

            if (recordatorios.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.notifications_scheduled_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(recordatorios.sortedBy { it.programadoEnMillis }, key = { it.id }) { recordatorio ->
                    TarjetaNotificacion(
                        recordatorio = recordatorio,
                        tramite = tramites.firstOrNull { it.id == recordatorio.tramiteId },
                        onBorrar = { onBorrarRecordatorio(recordatorio) }
                    )
                }
            }
        }

        if (mostrarAvisoInicial) {
            AvisoInicialNotificaciones(onDescartarAviso = onDescartarAviso)
        }
    }
}

internal fun filtrarTramitesNotificaciones(
    tramites: List<Tramite>,
    consulta: String
): List<Tramite> {
    val texto = consulta.trim()
    if (texto.isBlank()) return tramites
    return tramites.filter {
        it.title.contains(texto, ignoreCase = true) ||
            it.institution.contains(texto, ignoreCase = true)
    }
}

internal fun combinarFechaHora(
    fecha: LocalDate,
    hora: Int,
    minuto: Int,
    zona: ZoneId
): Long = fecha
    .atTime(hora, minuto)
    .atZone(zona)
    .toInstant()
    .toEpochMilli()

@Composable
private fun EncabezadoNotificaciones(
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
                    contentDescription = stringResource(R.string.notifications_cd_back),
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
            }
            Text(
                text = stringResource(R.string.notifications_title),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioNotificacion(
    tramites: List<Tramite>,
    textoBusqueda: String,
    tramiteSeleccionadoId: String?,
    titulo: String,
    nota: String,
    fechaSeleccionada: LocalDate?,
    horaSeleccionada: Int?,
    minutoSeleccionado: Int?,
    selectorActivo: SelectorFechaHora,
    mostrarErrores: Boolean,
    fechaHoraInvalida: Boolean,
    onTextoBusquedaChange: (String) -> Unit,
    onSeleccionarTramite: (Tramite) -> Unit,
    onLimpiarTramite: () -> Unit,
    onTituloChange: (String) -> Unit,
    onNotaChange: (String) -> Unit,
    onAbrirFecha: () -> Unit,
    onCancelarSelector: () -> Unit,
    onConfirmarFecha: (LocalDate) -> Unit,
    onConfirmarHora: (Int, Int) -> Unit,
    onGuardar: () -> Unit
) {
    var menuExpandido by remember { mutableStateOf(false) }
    val tramitesFiltrados = remember(tramites, textoBusqueda, tramiteSeleccionadoId) {
        if (tramiteSeleccionadoId == null) {
            filtrarTramitesNotificaciones(tramites, textoBusqueda)
        } else {
            emptyList()
        }
    }
    val errorTramite = mostrarErrores && tramiteSeleccionadoId == null
    val errorTitulo = mostrarErrores && titulo.isBlank()
    val errorFecha = mostrarErrores && fechaSeleccionada == null
    val errorHora = mostrarErrores && (horaSeleccionada == null || minutoSeleccionado == null)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.notifications_new_reminder),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )

        ExposedDropdownMenuBox(
            expanded = menuExpandido && tramitesFiltrados.isNotEmpty(),
            onExpandedChange = { menuExpandido = it }
        ) {
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = {
                    onTextoBusquedaChange(it)
                    menuExpandido = true
                },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                    .fillMaxWidth(),
                label = { Text(stringResource(R.string.notifications_select_procedure)) },
                placeholder = { Text(stringResource(R.string.notifications_search_placeholder)) },
                singleLine = true,
                isError = errorTramite,
                supportingText = if (errorTramite) {
                    { Text(stringResource(R.string.notifications_error_procedure)) }
                } else {
                    null
                },
                trailingIcon = {
                    if (tramiteSeleccionadoId != null) {
                        IconButton(onClick = onLimpiarTramite) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = stringResource(R.string.notifications_cd_clear_procedure),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido)
                    }
                },
                colors = coloresCampoNotificacion()
            )
            ExposedDropdownMenu(
                expanded = menuExpandido && tramitesFiltrados.isNotEmpty(),
                onDismissRequest = { menuExpandido = false }
            ) {
                tramitesFiltrados.forEach { tramite ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(tramite.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    tramite.institution,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        onClick = {
                            onSeleccionarTramite(tramite)
                            menuExpandido = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = titulo,
            onValueChange = onTituloChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.notifications_reminder_title)) },
            singleLine = true,
            isError = errorTitulo,
            supportingText = if (errorTitulo) {
                { Text(stringResource(R.string.notifications_error_title)) }
            } else {
                null
            },
            colors = coloresCampoNotificacion()
        )

        OutlinedTextField(
            value = nota,
            onValueChange = onNotaChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp),
            label = { Text(stringResource(R.string.notifications_note)) },
            maxLines = 3,
            colors = coloresCampoNotificacion()
        )

        CampoFechaNotificacion(
            fechaSeleccionada = fechaSeleccionada,
            horaSeleccionada = horaSeleccionada,
            minutoSeleccionado = minutoSeleccionado,
            isError = errorFecha || errorHora || fechaHoraInvalida,
            fechaHoraInvalida = fechaHoraInvalida,
            onAbrirFecha = onAbrirFecha
        )

        when (selectorActivo) {
            SelectorFechaHora.Fecha -> SelectorFechaNotificacion(
                fechaInicial = fechaSeleccionada,
                onCancelar = onCancelarSelector,
                onConfirmar = onConfirmarFecha
            )

            SelectorFechaHora.Hora -> SelectorHoraNotificacion(
                horaInicial = horaSeleccionada,
                minutoInicial = minutoSeleccionado,
                onCancelar = onCancelarSelector,
                onConfirmar = onConfirmarHora
            )

            SelectorFechaHora.Ninguno -> Unit
        }

        Button(
            onClick = onGuardar,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(
                text = stringResource(R.string.notifications_save),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CampoFechaNotificacion(
    fechaSeleccionada: LocalDate?,
    horaSeleccionada: Int?,
    minutoSeleccionado: Int?,
    isError: Boolean,
    fechaHoraInvalida: Boolean,
    onAbrirFecha: () -> Unit
) {
    val fechaTexto = fechaSeleccionada?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).orEmpty()
    val horaTexto = if (horaSeleccionada != null && minutoSeleccionado != null) {
        String.format(Locale.ROOT, "%02d:%02d", horaSeleccionada, minutoSeleccionado)
    } else {
        ""
    }
    OutlinedTextField(
        value = listOf(fechaTexto, horaTexto).filter { it.isNotBlank() }.joinToString("  "),
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAbrirFecha),
        readOnly = true,
        label = { Text(stringResource(R.string.notifications_date)) },
        placeholder = { Text(stringResource(R.string.notifications_date_placeholder)) },
        trailingIcon = {
            IconButton(onClick = onAbrirFecha) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = stringResource(R.string.notifications_cd_open_date)
                )
            }
        },
        isError = isError,
        supportingText = {
            Text(
                when {
                    fechaHoraInvalida -> stringResource(R.string.notifications_error_future)
                    isError -> stringResource(R.string.notifications_error_date_time)
                    else -> stringResource(R.string.notifications_date_format)
                }
            )
        },
        colors = coloresCampoNotificacion()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorFechaNotificacion(
    fechaInicial: LocalDate?,
    onCancelar: () -> Unit,
    onConfirmar: (LocalDate) -> Unit
) {
    val fechaBase = fechaInicial ?: LocalDate.now().plusDays(1)
    val estadoFecha = rememberDatePickerState(
        initialSelectedDateMillis = fechaBase
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(horizontalAlignment = Alignment.End) {
            DatePicker(
                state = estadoFecha,
                modifier = Modifier.fillMaxWidth(),
                title = null,
                headline = null,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedDayContainerColor = MaterialTheme.colorScheme.secondary,
                    selectedDayContentColor = MaterialTheme.colorScheme.onSecondary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary
                )
            )
            FilaAccionesSelector(
                onCancelar = onCancelar,
                onConfirmar = {
                    estadoFecha.selectedDateMillis?.let { millis ->
                        onConfirmar(
                            Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorHoraNotificacion(
    horaInicial: Int?,
    minutoInicial: Int?,
    onCancelar: () -> Unit,
    onConfirmar: (Int, Int) -> Unit
) {
    val estadoHora = rememberTimePickerState(
        initialHour = horaInicial ?: 9,
        initialMinute = minutoInicial ?: 0,
        is24Hour = false
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.notifications_enter_time),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            TimeInput(
                state = estadoHora,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = stringResource(R.string.notifications_cd_time),
                    tint = MaterialTheme.colorScheme.primary
                )
                Box(modifier = Modifier.weight(1f))
                FilaAccionesSelector(
                    onCancelar = onCancelar,
                    onConfirmar = { onConfirmar(estadoHora.hour, estadoHora.minute) }
                )
            }
        }
    }
}

@Composable
private fun FilaAccionesSelector(
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onCancelar) {
            Text(stringResource(R.string.notifications_cancel))
        }
        TextButton(onClick = onConfirmar) {
            Text(stringResource(R.string.notifications_ok))
        }
    }
}

@Composable
private fun TarjetaNotificacion(
    recordatorio: Recordatorio,
    tramite: Tramite?,
    onBorrar: () -> Unit
) {
    val fecha = Instant.ofEpochMilli(recordatorio.programadoEnMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm", Locale.forLanguageTag("es-EC")))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = stringResource(R.string.notifications_cd_scheduled),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = recordatorio.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tramite?.title ?: stringResource(R.string.notifications_procedure_fallback),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = fecha,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (recordatorio.notes.isNotBlank()) {
                    Text(recordatorio.notes, style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onBorrar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.notifications_cd_delete)
                )
            }
        }
    }
}

@Composable
private fun AvisoCuentaNecesaria() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Text(
            text = stringResource(R.string.notifications_guest_message),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvisoInicialNotificaciones(onDescartarAviso: () -> Unit) {
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
                contentDescription = stringResource(R.string.notifications_cd_welcome_help),
                modifier = Modifier.size(58.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.notifications_welcome_help),
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

@Composable
private fun coloresCampoNotificacion() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    errorBorderColor = MaterialTheme.colorScheme.error,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    errorLabelColor = MaterialTheme.colorScheme.error
)

@Preview(name = "Notificaciones compacta", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewPantallaNotificacionesCompacta() {
    TemaAsistenTED(darkTheme = false) {
        PantallaNotificacionesPreview()
    }
}

@Preview(name = "Notificaciones con aviso", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewPantallaNotificacionesConAviso() {
    TemaAsistenTED(darkTheme = false) {
        PantallaNotificacionesPreview(mostrarAvisoInicial = true)
    }
}

@Preview(name = "Notificaciones amplia", showBackground = true, widthDp = 720, heightDp = 1000)
@Composable
private fun PreviewPantallaNotificacionesAmplia() {
    TemaAsistenTED(darkTheme = false) {
        PantallaNotificacionesPreview()
    }
}

@Composable
private fun PantallaNotificacionesPreview(mostrarAvisoInicial: Boolean = false) {
    val tramite = CatalogoTramites.tramites.first()
    PantallaNotificaciones(
        tramites = CatalogoTramites.tramites,
        recordatorios = listOf(
            Recordatorio(
                id = "preview",
                tramiteId = tramite.id,
                title = "Renovar documento",
                notes = "Revisar los requisitos antes de iniciar.",
                programadoEnMillis = LocalDateTime.now()
                    .plusDays(2)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            )
        ),
        esInvitado = false,
        mostrarAvisoInicial = mostrarAvisoInicial,
        onRegresar = {},
        onAbrirPerfil = {},
        onGuardarRecordatorio = { _, _, _, _ -> },
        onBorrarRecordatorio = {},
        onDescartarAviso = {}
    )
}
