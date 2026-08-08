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
import androidx.compose.foundation.layout.heightIn
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
internal fun AvatarUsuario(
    avatarId: String,
    modifier: Modifier = Modifier
) {
    val avatar = avataresPerfil.firstOrNull { it.id == avatarId } ?: avataresPerfil.first()
    ImagenAvatar(
        avatar = avatar,
        modifier = modifier,
        seleccionado = false,
        mostrarMarca = false
    )
}

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
                .widthIn(max = DimensionesDiseno.anchoContenido)
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
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            CabeceraPerfil(
                titulo = stringResource(R.string.profile_title),
                avatar = avatar,
                onRegresar = onRegresar
            )
        }
        item {
            ResumenPerfilConsulta(perfil = perfil, avatar = avatar)
        }
        if (esInvitado) {
            item {
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text(
                        text = stringResource(R.string.profile_guest_message),
                        modifier = Modifier.padding(DimensionesDiseno.paddingTarjeta),
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
            TarjetaProposito()
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
        contentPadding = PaddingValues(bottom = 24.dp),
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
                        .height(DimensionesDiseno.altoAccion),
                    shape = MaterialTheme.shapes.small,
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onRegresar,
                modifier = Modifier.size(DimensionesDiseno.objetivoTactil)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.profile_cd_back),
                    modifier = Modifier.size(DimensionesDiseno.iconoAccion)
                )
            }
            Text(
                text = titulo,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Box(
                modifier = Modifier
                    .size(DimensionesDiseno.objetivoTactil)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                ImagenAvatar(
                    avatar = avatar,
                    modifier = Modifier.size(DimensionesDiseno.avatarEncabezado),
                    seleccionado = false,
                    mostrarMarca = false
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun ResumenPerfilConsulta(
    perfil: PerfilUsuario?,
    avatar: AvatarPerfilVisual
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ImagenAvatar(
            avatar = avatar,
            modifier = Modifier.size(82.dp),
            seleccionado = true,
            mostrarMarca = false
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = perfil?.nombreVisible.orEmpty().ifBlank { stringResource(R.string.profile_guest_name) },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.profile_username, perfil?.username.orEmpty()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
        shape = MaterialTheme.shapes.small,
        isError = isError,
        supportingText = if (isError) ({ Text(mensajeError) }) else null,
        colors = coloresCampoDiseno()
    )
}

@Composable
private fun SelectorAvataresPerfil(
    avatarSeleccionadoId: String,
    onSeleccionar: (AvatarPerfilVisual) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
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
            .heightIn(min = DimensionesDiseno.objetivoTactil)
            .padding(vertical = 12.dp),
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
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = DimensionesDiseno.objetivoTactil),
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
private fun TarjetaProposito() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = stringResource(R.string.profile_purpose),
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Start
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvisoInicialPerfil(onDescartar: () -> Unit) {
    AvisoModalDiseno(
        mensaje = stringResource(R.string.profile_welcome_help),
        textoBoton = stringResource(R.string.home_do_not_remind),
        descripcionIcono = stringResource(R.string.profile_cd_welcome_help),
        onContinuar = onDescartar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvisoPerfilGuardado(onContinuar: () -> Unit) {
    AvisoModalDiseno(
        mensaje = stringResource(R.string.profile_changes_saved),
        textoBoton = stringResource(R.string.profile_continue),
        descripcionIcono = stringResource(R.string.profile_cd_saved),
        onContinuar = onContinuar,
        iconoConfirmacion = true
    )
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

@Preview(name = "Perfil alto contraste y texto grande", showBackground = true, widthDp = 320, heightDp = 800)
@Composable
private fun PreviewPerfilAccesible() {
    val configuracion = ConfiguracionAccesibilidad(textoGrande = true, altoContraste = true)
    TemaAsistenTED(darkTheme = false, configuracionAccesibilidad = configuracion) {
        PerfilPreviewBase(
            accesibilidadExpandida = true,
            configuracion = configuracion
        )
    }
}

@Composable
private fun PerfilPreviewBase(
    modoInicial: ModoPantallaPerfil = ModoPantallaPerfil.Consulta,
    mostrarAviso: Boolean = false,
    accesibilidadExpandida: Boolean = false,
    mostrarConfirmacion: Boolean = false,
    configuracion: ConfiguracionAccesibilidad = ConfiguracionAccesibilidad()
) {
    PantallaPerfilBase(
        perfil = perfilPreview,
        configuracion = configuracion,
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
