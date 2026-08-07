package com.asistented.app.interfaz

import androidx.annotation.DrawableRes
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.asistented.app.datos.modelos.ConfiguracionAccesibilidad
import com.asistented.app.datos.modelos.PerfilUsuario
import com.asistented.app.interfaz.tema.TemaAsistenTED

private enum class ModoPantallaPerfil {
    Consulta,
    Edicion
}

private data class AvatarPerfilVisual(
    val id: String,
    @param:DrawableRes val recurso: Int,
    val indiceAccesible: Int
)

private val avataresPerfil = listOf(
    AvatarPerfilVisual("azul", R.drawable.avatar_perfil_1, 1),
    AvatarPerfilVisual("avatar_2", R.drawable.avatar_perfil_2, 2),
    AvatarPerfilVisual("avatar_3", R.drawable.avatar_perfil_3, 3),
    AvatarPerfilVisual("avatar_4", R.drawable.avatar_perfil_4, 4),
    AvatarPerfilVisual("avatar_5", R.drawable.avatar_perfil_5, 5),
    AvatarPerfilVisual("avatar_6", R.drawable.avatar_perfil_6, 6),
    AvatarPerfilVisual("avatar_7", R.drawable.avatar_perfil_7, 7),
    AvatarPerfilVisual("avatar_8", R.drawable.avatar_perfil_8, 8),
    AvatarPerfilVisual("avatar_9", R.drawable.avatar_perfil_9, 9)
)

@Composable
internal fun PantallaPerfilRedisenada(
    perfil: PerfilUsuario?,
    configuracion: ConfiguracionAccesibilidad,
    mostrarAvisoInicial: Boolean,
    onRegresar: () -> Unit,
    onGuardarPerfil: (String, String, String, (Boolean) -> Unit) -> Unit,
    onActualizarAccesibilidad: (ConfiguracionAccesibilidad) -> Unit,
    onAbrirHistorial: () -> Unit,
    onCerrarSesion: () -> Unit,
    onDescartarAviso: () -> Unit,
    modifier: Modifier = Modifier
) {
    PantallaPerfilBase(
        perfil = perfil,
        configuracion = configuracion,
        mostrarAvisoInicial = mostrarAvisoInicial,
        onRegresar = onRegresar,
        onGuardarPerfil = onGuardarPerfil,
        onActualizarAccesibilidad = onActualizarAccesibilidad,
        onAbrirHistorial = onAbrirHistorial,
        onCerrarSesion = onCerrarSesion,
        onDescartarAviso = onDescartarAviso,
        modifier = modifier
    )
}

@Composable
private fun PantallaPerfilBase(
    perfil: PerfilUsuario?,
    configuracion: ConfiguracionAccesibilidad,
    mostrarAvisoInicial: Boolean,
    onRegresar: () -> Unit,
    onGuardarPerfil: (String, String, String, (Boolean) -> Unit) -> Unit,
    onActualizarAccesibilidad: (ConfiguracionAccesibilidad) -> Unit,
    onAbrirHistorial: () -> Unit,
    onCerrarSesion: () -> Unit,
    onDescartarAviso: () -> Unit,
    modifier: Modifier = Modifier,
    modoInicial: ModoPantallaPerfil = ModoPantallaPerfil.Consulta,
    accesibilidadInicialExpandida: Boolean = false,
    confirmacionInicial: Boolean = false
) {
    val avatarInicial = avataresPerfil.firstOrNull { it.id == perfil?.avatarId } ?: avataresPerfil.first()
    var modo by rememberSaveable(perfil?.uid) { mutableStateOf(modoInicial) }
    var nombre by rememberSaveable(perfil?.uid) { mutableStateOf(perfil?.nombre.orEmpty()) }
    var apellido by rememberSaveable(perfil?.uid) { mutableStateOf(perfil?.apellido.orEmpty()) }
    var avatarSeleccionadoId by rememberSaveable(perfil?.uid) { mutableStateOf(avatarInicial.id) }
    var accesibilidadExpandida by rememberSaveable { mutableStateOf(accesibilidadInicialExpandida) }
    var intentoGuardar by rememberSaveable { mutableStateOf(false) }
    var mostrarConfirmacion by rememberSaveable { mutableStateOf(confirmacionInicial) }
    val esInvitado = perfil?.esInvitado == true
    val avatarSeleccionado = avataresPerfil.firstOrNull { it.id == avatarSeleccionadoId } ?: avataresPerfil.first()
    val cerrarEdicion = {
        intentoGuardar = false
        modo = ModoPantallaPerfil.Consulta
    }

    BackHandler(enabled = modo == ModoPantallaPerfil.Edicion) { cerrarEdicion() }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 720.dp)
                .fillMaxSize()
        ) {
            if (modo == ModoPantallaPerfil.Consulta) {
                ContenidoPerfilConsulta(
                    perfil = perfil,
                    avatar = avatarSeleccionado,
                    configuracion = configuracion,
                    esInvitado = esInvitado,
                    accesibilidadExpandida = accesibilidadExpandida,
                    onRegresar = onRegresar,
                    onEditar = { if (!esInvitado) modo = ModoPantallaPerfil.Edicion },
                    onAlternarAccesibilidad = { accesibilidadExpandida = !accesibilidadExpandida },
                    onActualizarAccesibilidad = onActualizarAccesibilidad,
                    onAbrirHistorial = onAbrirHistorial,
                    onCerrarSesion = onCerrarSesion
                )
            } else {
                ContenidoEdicionPerfil(
                    nombre = nombre,
                    apellido = apellido,
                    avatarSeleccionado = avatarSeleccionado,
                    intentoGuardar = intentoGuardar,
                    onRegresar = cerrarEdicion,
                    onNombreChange = { nombre = it },
                    onApellidoChange = { apellido = it },
                    onSeleccionarAvatar = { avatarSeleccionadoId = it.id },
                    onGuardar = {
                        intentoGuardar = true
                        if (nombre.isNotBlank() && apellido.isNotBlank()) {
                            onGuardarPerfil(nombre, apellido, avatarSeleccionado.id) { guardado ->
                                if (guardado) mostrarConfirmacion = true
                            }
                        }
                    }
                )
            }
        }

        // La ayuda inicial tiene prioridad para que una cuenta nueva conozca la pantalla antes de editar.
        when {
            mostrarAvisoInicial -> AvisoInicialPerfil(onDescartarAviso)
            mostrarConfirmacion -> AvisoPerfilGuardado {
                mostrarConfirmacion = false
                intentoGuardar = false
                modo = ModoPantallaPerfil.Consulta
            }
        }
    }
}

@Composable
private fun ContenidoPerfilConsulta(
    perfil: PerfilUsuario?,
    avatar: AvatarPerfilVisual,
    configuracion: ConfiguracionAccesibilidad,
    esInvitado: Boolean,
    accesibilidadExpandida: Boolean,
    onRegresar: () -> Unit,
    onEditar: () -> Unit,
    onAlternarAccesibilidad: () -> Unit,
    onActualizarAccesibilidad: (ConfiguracionAccesibilidad) -> Unit,
    onAbrirHistorial: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 18.dp)
    ) {
        item {
            CabeceraPerfil(
                titulo = stringResource(R.string.profile_title),
                avatar = avatar,
                onRegresar = onRegresar
            )
        }
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = perfil?.nombreVisible.orEmpty().ifBlank { stringResource(R.string.profile_guest_name) },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.profile_username, perfil?.username.orEmpty()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
            }
        }
        if (esInvitado) {
            item {
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text(
                        text = stringResource(R.string.profile_guest_message),
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OpcionPerfil(
                    icono = Icons.Default.Edit,
                    texto = stringResource(R.string.profile_edit),
                    descripcion = stringResource(R.string.profile_cd_edit),
                    habilitada = !esInvitado,
                    onClick = onEditar
                )
                OpcionPerfil(
                    icono = Icons.Default.Settings,
                    texto = stringResource(R.string.profile_accessibility),
                    descripcion = stringResource(R.string.profile_cd_accessibility),
                    expandida = accesibilidadExpandida,
                    onClick = onAlternarAccesibilidad
                )
                if (accesibilidadExpandida) {
                    PanelAccesibilidad(
                        configuracion = configuracion,
                        onActualizar = onActualizarAccesibilidad
                    )
                }
                OpcionPerfil(
                    icono = Icons.Default.History,
                    texto = stringResource(R.string.profile_history),
                    descripcion = stringResource(R.string.profile_cd_history),
                    onClick = onAbrirHistorial
                )
                OpcionPerfil(
                    icono = Icons.AutoMirrored.Filled.Logout,
                    texto = stringResource(R.string.profile_logout),
                    descripcion = stringResource(R.string.profile_cd_logout),
                    colorContenido = MaterialTheme.colorScheme.error,
                    onClick = onCerrarSesion
                )
                HorizontalDivider()
            }
        }
        item {
            TarjetaProposito(avatar = avatar)
        }
    }
}

@Composable
private fun ContenidoEdicionPerfil(
    nombre: String,
    apellido: String,
    avatarSeleccionado: AvatarPerfilVisual,
    intentoGuardar: Boolean,
    onRegresar: () -> Unit,
    onNombreChange: (String) -> Unit,
    onApellidoChange: (String) -> Unit,
    onSeleccionarAvatar: (AvatarPerfilVisual) -> Unit,
    onGuardar: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CabeceraPerfil(
                titulo = stringResource(R.string.profile_edit_title),
                avatar = avatarSeleccionado,
                onRegresar = onRegresar
            )
        }
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoPerfil(
                    valor = nombre,
                    onValorChange = onNombreChange,
                    etiqueta = stringResource(R.string.profile_new_name),
                    mensajeError = stringResource(R.string.profile_error_name),
                    isError = intentoGuardar && nombre.isBlank()
                )
                CampoPerfil(
                    valor = apellido,
                    onValorChange = onApellidoChange,
                    etiqueta = stringResource(R.string.profile_new_last_name),
                    mensajeError = stringResource(R.string.profile_error_last_name),
                    isError = intentoGuardar && apellido.isBlank()
                )
                Text(
                    text = stringResource(R.string.profile_choose_avatar),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                SelectorAvataresPerfil(
                    avatarSeleccionadoId = avatarSeleccionado.id,
                    onSeleccionar = onSeleccionarAvatar
                )
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
                    Text(stringResource(R.string.profile_save_changes), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CabeceraPerfil(
    titulo: String,
    avatar: AvatarPerfilVisual,
    onRegresar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp),
            color = MaterialTheme.colorScheme.secondary
        ) {
            Box {
                IconButton(
                    onClick = onRegresar,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 4.dp, top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.profile_cd_back)
                    )
                }
                Text(
                    text = titulo,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 17.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        ImagenAvatar(
            avatar = avatar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(90.dp),
            seleccionado = true,
            mostrarMarca = false
        )
    }
}

@Composable
private fun CampoPerfil(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    mensajeError: String,
    isError: Boolean
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        singleLine = true,
        isError = isError,
        supportingText = if (isError) ({ Text(mensajeError) }) else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            errorLabelColor = MaterialTheme.colorScheme.error
        )
    )
}

@Composable
private fun SelectorAvataresPerfil(
    avatarSeleccionadoId: String,
    onSeleccionar: (AvatarPerfilVisual) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Las filas con pesos iguales se adaptan al ancho disponible sin coordenadas absolutas.
            avataresPerfil.chunked(3).forEach { filaAvatares ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    filaAvatares.forEach { avatar ->
                        ImagenAvatar(
                            avatar = avatar,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clickable { onSeleccionar(avatar) },
                            seleccionado = avatar.id == avatarSeleccionadoId
                        )
                    }
                    repeat(3 - filaAvatares.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun ImagenAvatar(
    avatar: AvatarPerfilVisual,
    modifier: Modifier = Modifier,
    seleccionado: Boolean,
    mostrarMarca: Boolean = seleccionado
) {
    val borde = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(if (seleccionado) 3.dp else 1.dp, borde, CircleShape)
            .padding(if (seleccionado) 3.dp else 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(avatar.recurso),
            contentDescription = stringResource(R.string.profile_cd_avatar, avatar.indiceAccesible),
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        if (mostrarMarca) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.profile_cd_selected_avatar),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
                    .padding(3.dp),
                tint = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}

@Composable
private fun OpcionPerfil(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    descripcion: String,
    onClick: () -> Unit,
    habilitada: Boolean = true,
    expandida: Boolean? = null,
    colorContenido: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    val alphaContenido = if (habilitada) 1f else 0.4f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = habilitada, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icono, contentDescription = descripcion, tint = colorContenido.copy(alpha = alphaContenido))
        Text(
            text = texto,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = colorContenido.copy(alpha = alphaContenido)
        )
        Icon(
            imageVector = when (expandida) {
                true -> Icons.Default.KeyboardArrowUp
                false -> Icons.Default.KeyboardArrowDown
                null -> Icons.AutoMirrored.Filled.KeyboardArrowRight
            },
            contentDescription = descripcion,
            tint = colorContenido.copy(alpha = alphaContenido)
        )
    }
}

@Composable
private fun PanelAccesibilidad(
    configuracion: ConfiguracionAccesibilidad,
    onActualizar: (ConfiguracionAccesibilidad) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 36.dp, end = 4.dp, bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OpcionAccesibilidad(
            texto = stringResource(R.string.profile_large_text),
            activada = configuracion.textoGrande,
            onCambio = { onActualizar(configuracion.copy(textoGrande = it)) }
        )
        OpcionAccesibilidad(
            texto = stringResource(R.string.profile_high_contrast),
            activada = configuracion.altoContraste,
            onCambio = { onActualizar(configuracion.copy(altoContraste = it)) }
        )
    }
}

@Composable
private fun OpcionAccesibilidad(
    texto: String,
    activada: Boolean,
    onCambio: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(texto, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Switch(
            checked = activada,
            onCheckedChange = onCambio,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedTrackColor = MaterialTheme.colorScheme.tertiaryContainer,
                uncheckedBorderColor = MaterialTheme.colorScheme.tertiary
            )
        )
    }
}

@Composable
private fun TarjetaProposito(avatar: AvatarPerfilVisual) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ImagenAvatar(
            avatar = avatar,
            modifier = Modifier.size(64.dp),
            seleccionado = false
        )
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text(
                text = stringResource(R.string.profile_purpose),
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvisoInicialPerfil(onDescartar: () -> Unit) {
    val estadoHoja = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )
    ModalBottomSheet(
        onDismissRequest = {},
        sheetState = estadoHoja,
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
        dragHandle = null
    ) {
        ContenidoAvisoPerfil(
            icono = Icons.Default.SentimentSatisfiedAlt,
            descripcionIcono = stringResource(R.string.profile_cd_welcome_help),
            mensaje = stringResource(R.string.profile_welcome_help),
            textoBoton = stringResource(R.string.home_do_not_remind),
            onContinuar = onDescartar
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvisoPerfilGuardado(onContinuar: () -> Unit) {
    val estadoHoja = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )
    ModalBottomSheet(
        onDismissRequest = {},
        sheetState = estadoHoja,
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
        dragHandle = null
    ) {
        ContenidoAvisoPerfil(
            icono = Icons.Default.Check,
            descripcionIcono = stringResource(R.string.profile_cd_saved),
            mensaje = stringResource(R.string.profile_changes_saved),
            textoBoton = stringResource(R.string.profile_continue),
            onContinuar = onContinuar
        )
    }
}

@Composable
private fun ContenidoAvisoPerfil(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    descripcionIcono: String,
    mensaje: String,
    textoBoton: String,
    onContinuar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = descripcionIcono,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = mensaje,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onContinuar,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 270.dp)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(textoBoton, style = MaterialTheme.typography.labelMedium)
        }
    }
}

private val perfilPreview = PerfilUsuario(
    uid = "preview",
    username = "Alex123",
    nombre = "Alexis",
    apellido = "Rodríguez",
    avatarId = "azul"
)

@Preview(name = "Perfil", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewPerfil() {
    TemaAsistenTED(darkTheme = false) { PerfilPreviewBase() }
}

@Preview(name = "Editar perfil", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewEditarPerfil() {
    TemaAsistenTED(darkTheme = false) { PerfilPreviewBase(modoInicial = ModoPantallaPerfil.Edicion) }
}

@Preview(name = "Perfil con ayuda", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewPerfilConAyuda() {
    TemaAsistenTED(darkTheme = false) { PerfilPreviewBase(mostrarAviso = true) }
}

@Preview(name = "Perfil guardado", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewPerfilGuardado() {
    TemaAsistenTED(darkTheme = false) { PerfilPreviewBase(mostrarConfirmacion = true) }
}

@Preview(name = "Perfil accesibilidad amplia", showBackground = true, widthDp = 720, heightDp = 900)
@Composable
private fun PreviewPerfilAmplio() {
    TemaAsistenTED(darkTheme = false) { PerfilPreviewBase(accesibilidadExpandida = true) }
}

@Composable
private fun PerfilPreviewBase(
    modoInicial: ModoPantallaPerfil = ModoPantallaPerfil.Consulta,
    mostrarAviso: Boolean = false,
    accesibilidadExpandida: Boolean = false,
    mostrarConfirmacion: Boolean = false
) {
    PantallaPerfilBase(
        perfil = perfilPreview,
        configuracion = ConfiguracionAccesibilidad(),
        mostrarAvisoInicial = mostrarAviso,
        onRegresar = {},
        onGuardarPerfil = { _, _, _, resultado -> resultado(true) },
        onActualizarAccesibilidad = {},
        onAbrirHistorial = {},
        onCerrarSesion = {},
        onDescartarAviso = {},
        modoInicial = modoInicial,
        accesibilidadInicialExpandida = accesibilidadExpandida,
        confirmacionInicial = mostrarConfirmacion
    )
}
