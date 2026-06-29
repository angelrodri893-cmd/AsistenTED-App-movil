package com.asistented.app.interfaz

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.asistented.app.datos.RepositorioAutenticacion
import com.asistented.app.datos.RepositorioForo
import com.asistented.app.datos.PreferenciasLocales
import com.asistented.app.datos.CatalogoTramites
import com.asistented.app.datos.RepositorioUsuario
import com.asistented.app.datos.modelos.ConfiguracionAccesibilidad
import com.asistented.app.datos.modelos.ComentarioForo
import com.asistented.app.datos.modelos.ElementoHistorial
import com.asistented.app.datos.modelos.Tramite
import com.asistented.app.datos.modelos.Recordatorio
import com.asistented.app.datos.modelos.PerfilUsuario
import com.asistented.app.dominio.ReglasAutenticacion
import com.asistented.app.dominio.ReglasProgreso
import com.asistented.app.dominio.ReglasContenidoUsuario
import com.asistented.app.notificaciones.ProgramadorRecordatorios
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import java.util.UUID

class ControladorAsistenTed(context: Context) {
    private val appContext = context.applicationContext
    private val preferenciasLocales = PreferenciasLocales(appContext)
    private val repositorioAutenticacion = RepositorioAutenticacion()
    private val repositorioUsuario = RepositorioUsuario()
    private val repositorioForo = RepositorioForo()
    private val programadorRecordatorios = ProgramadorRecordatorios(appContext)

    val tramites = CatalogoTramites.tramites
    val favoritos = mutableStateListOf<String>()
    val historial = mutableStateListOf<ElementoHistorial>()
    val recordatorios = mutableStateListOf<Recordatorio>()
    val comentarios = mutableStateMapOf<String, List<ComentarioForo>>()
    val pasosCompletados = mutableStateMapOf<String, Set<String>>()

    var usuarioActual by mutableStateOf<PerfilUsuario?>(null)
        private set
    var configuracionAccesibilidad by mutableStateOf(preferenciasLocales.cargarAccesibilidad())
        private set
    var cargando by mutableStateOf(false)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set

    init {
        repositorioAutenticacion.usuarioActual { perfil ->
            perfil?.let { establecerUsuarioRegistrado(it) }
        }
    }

    fun limpiarMensaje() {
        mensaje = null
    }

    fun mostrarMensaje(text: String) {
        mensaje = text
    }

    fun entrarComoInvitado() {
        usuarioActual = repositorioAutenticacion.perfilInvitado().copy(accessibility = configuracionAccesibilidad)
        favoritos.clear()
        historial.clear()
        recordatorios.clear()
        mensaje = "Entraste como anónimo. Puedes ver guías, pero algunas funciones se guardan solo con cuenta."
    }

    fun registrar(username: String, nombre: String, apellido: String, password: String, confirmation: String) {
        val usernameError = ReglasAutenticacion.validarUsuario(username)
        val passwordError = ReglasAutenticacion.validarContrasena(password, confirmation)
        if (usernameError != null || passwordError != null) {
            mensaje = usernameError ?: passwordError
            return
        }
        cargando = true
        repositorioAutenticacion.registrar(username, nombre, apellido, password) { result ->
            cargando = false
            result
                .onSuccess {
                    establecerUsuarioRegistrado(it)
                    mensaje = "Cuenta creada correctamente."
                }
                .onFailure { mensaje = it.comoMensajeUsuario("No se pudo crear la cuenta.") }
        }
    }

    fun iniciarSesion(username: String, password: String) {
        val usernameError = ReglasAutenticacion.validarUsuario(username)
        val passwordError = ReglasAutenticacion.validarContrasena(password)
        if (usernameError != null || passwordError != null) {
            mensaje = usernameError ?: passwordError
            return
        }
        cargando = true
        repositorioAutenticacion.iniciarSesion(username, password) { result ->
            cargando = false
            result
                .onSuccess {
                    establecerUsuarioRegistrado(it)
                    mensaje = "Sesión iniciada."
                }
                .onFailure { mensaje = it.comoMensajeUsuario("Usuario o contraseña incorrectos.") }
        }
    }

    fun cerrarSesion() {
        repositorioAutenticacion.cerrarSesion()
        usuarioActual = null
        favoritos.clear()
        historial.clear()
        recordatorios.clear()
        pasosCompletados.clear()
        comentarios.clear()
    }

    fun actualizarAccesibilidad(configuracion: ConfiguracionAccesibilidad) {
        configuracionAccesibilidad = configuracion
        preferenciasLocales.guardarAccesibilidad(configuracion)
        usuarioActual = usuarioActual?.copy(accessibility = configuracion)
        usuarioActual?.takeIf { !it.esInvitado }?.let { repositorioUsuario.guardarPerfil(it) }
    }

    fun actualizarPerfil(nombre: String, apellido: String, avatarId: String) {
        val user = usuarioActual ?: return
        if (user.esInvitado) {
            mensaje = "Crea una cuenta para guardar tu perfil."
            return
        }
        val updated = user.copy(
            nombre = nombre.trim(),
            apellido = apellido.trim(),
            avatarId = avatarId
        )
        usuarioActual = updated
        repositorioUsuario.guardarPerfil(updated) {
            mensaje = if (it.isSuccess) "Perfil guardado." else "No se pudo guardar el perfil."
        }
    }

    fun cargarComentarios(tramiteId: String) {
        repositorioForo.cargar(tramiteId) { loaded ->
            if (loaded.isNotEmpty()) {
                comentarios[tramiteId] = loaded
            }
        }
    }

    fun marcarConsultado(tramiteId: String) {
        val updated = ReglasContenidoUsuario.registrarHistorial(historial, tramiteId, System.currentTimeMillis())
        historial.clear()
        historial.addAll(updated)
        val item = updated.first()
        usuarioActual?.let { repositorioUsuario.agregarHistorial(it.uid, item) }
    }

    fun alternarFavorito(tramiteId: String) {
        val user = usuarioActual ?: return
        if (user.esInvitado) {
            mensaje = "Inicia sesión para guardar favoritos."
            return
        }
        val updated = ReglasContenidoUsuario.alternarFavorito(favoritos, tramiteId)
        val enabled = updated.contains(tramiteId)
        favoritos.clear()
        favoritos.addAll(updated)
        repositorioUsuario.guardarFavorito(user.uid, tramiteId, enabled)
    }

    fun alternarPaso(tramiteId: String, stepId: String) {
        val current = pasosCompletados[tramiteId].orEmpty()
        val updated = ReglasProgreso.alternarPaso(current, stepId)
        pasosCompletados[tramiteId] = updated
        usuarioActual?.let { repositorioUsuario.guardarProgreso(it.uid, tramiteId, updated) }
    }

    fun agregarComentario(tramiteId: String, text: String) {
        val user = usuarioActual ?: return
        if (user.esInvitado) {
            mensaje = "Inicia sesión para comentar en el foro."
            return
        }
        if (text.isBlank()) {
            mensaje = "Escribe tu pregunta o comentario."
            return
        }
        val comment = ComentarioForo(
            id = UUID.randomUUID().toString(),
            tramiteId = tramiteId,
            userId = user.uid,
            username = user.nombreVisible,
            text = text.trim(),
            createdAtMillis = System.currentTimeMillis()
        )
        comentarios[tramiteId] = comentarios[tramiteId].orEmpty() + comment
        repositorioForo.publicar(comment)
        mensaje = "Comentario publicado."
    }

    fun agregarRecordatorio(tramiteId: String, title: String, notes: String, programadoEnMillis: Long) {
        val user = usuarioActual ?: return
        if (user.esInvitado) {
            mensaje = "Inicia sesión para guardar recordatorios."
            return
        }
        if (programadoEnMillis <= System.currentTimeMillis()) {
            mensaje = "Elige una fecha y hora futura."
            return
        }
        val reminder = Recordatorio(
            id = UUID.randomUUID().toString(),
            tramiteId = tramiteId,
            title = title.ifBlank { "Recordatorio de trámite" },
            notes = notes,
            programadoEnMillis = programadoEnMillis
        )
        recordatorios.add(0, reminder)
        repositorioUsuario.guardarRecordatorio(user.uid, reminder)
        programadorRecordatorios.programar(reminder)
        mensaje = "Recordatorio guardado."
    }

    fun borrarRecordatorio(reminder: Recordatorio) {
        recordatorios.remove(reminder)
        programadorRecordatorios.cancelar(reminder)
        usuarioActual?.let { repositorioUsuario.borrarRecordatorio(it.uid, reminder.id) }
    }

    private fun establecerUsuarioRegistrado(perfil: PerfilUsuario) {
        val usuarioRegistrado = perfil.copy(accessibility = configuracionAccesibilidad)
        usuarioActual = usuarioRegistrado
        cargarDatosPersistidos(usuarioRegistrado.uid)
    }

    private fun cargarDatosPersistidos(uid: String) {
        repositorioUsuario.cargarFavoritos(uid) { loaded ->
            favoritos.clear()
            favoritos.addAll(loaded)
        }
        repositorioUsuario.cargarHistorial(uid) { loaded ->
            historial.clear()
            historial.addAll(loaded)
        }
        repositorioUsuario.cargarProgreso(uid) { loaded ->
            pasosCompletados.clear()
            pasosCompletados.putAll(loaded)
        }
        repositorioUsuario.cargarRecordatorios(uid) { loaded ->
            recordatorios.clear()
            recordatorios.addAll(loaded)
        }
    }
}

private fun Throwable.comoMensajeUsuario(defaultMessage: String): String {
    val raw = (localizedMessage ?: message).orEmpty()
    return when {
        raw.contains("PERMISSION_DENIED", ignoreCase = true) ||
            raw.contains("permission_denied", ignoreCase = true) ||
            raw.contains("Missing or insufficient permissions", ignoreCase = true) ->
            "Faltan permisos en Firestore. Publica las reglas de seguridad del proyecto y vuelve a intentar."
        else -> raw.ifBlank { defaultMessage }
    }
}

@Composable
fun AplicacionAsistenTed(controlador: ControladorAsistenTed) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(controlador.mensaje) {
        val current = controlador.mensaje
        if (current != null) {
            scope.launch { snackbarHostState.showSnackbar(current) }
            controlador.limpiarMensaje()
        }
    }

    if (controlador.usuarioActual == null) {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
            PantallaAutenticacion(
                controlador = controlador,
                modifier = Modifier.padding(padding)
            )
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BarraInferiorPrincipal(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Rutas.HOME) {
                PantallaInicio(controlador, navController)
            }
            composable(
                route = Rutas.DETAIL,
                arguments = listOf(navArgument("tramiteId") { type = NavType.StringType })
            ) { entry ->
                val tramiteId = entry.arguments?.getString("tramiteId").orEmpty()
                CatalogoTramites.buscarTramite(tramiteId)?.let {
                    PantallaDetalleTramite(controlador, it, navController)
                }
            }
            composable(Rutas.FAVORITES) {
                PantallaListaTramites(
                    title = "Favoritos",
                    emptyText = "Todavía no guardas trámites favoritos.",
                    tramites = controlador.tramites.filter { controlador.favoritos.contains(it.id) },
                    controlador = controlador,
                    navController = navController
                )
            }
            composable(Rutas.HISTORY) {
                val historialTramites = controlador.historial.mapNotNull { CatalogoTramites.buscarTramite(it.tramiteId) }
                PantallaListaTramites(
                    title = "Historial",
                    emptyText = "Aquí aparecerán los trámites que revises.",
                    tramites = historialTramites,
                    controlador = controlador,
                    navController = navController
                )
            }
            composable(Rutas.REMINDERS) {
                PantallaRecordatorios(controlador)
            }
            composable(Rutas.PROFILE) {
                PantallaPerfil(controlador)
            }
            composable(Rutas.ACCESSIBILITY) {
                PantallaAccesibilidad(controlador)
            }
        }
    }
}

@Composable
private fun PantallaAutenticacion(controlador: ControladorAsistenTed, modifier: Modifier = Modifier) {
    var registerMode by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("AsistenTED", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text(
            "Guía sencilla para trámites digitales del Ecuador.",
            style = cuerpoLegible(controlador)
        )
        TarjetaAviso(
            title = "Puedes entrar sin cuenta",
            text = "Como anónimo podrás revisar las guías. Para guardar favoritos, historial, perfil, foro y recordatorios necesitas registrarte."
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !registerMode, onClick = { registerMode = false }, label = { Text("Iniciar sesión") })
            FilterChip(selected = registerMode, onClick = { registerMode = true }, label = { Text("Registrarme") })
        }
        if (registerMode) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                supportingText = { Text("Escribe cómo quieres que te saludemos.") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = apellido,
                onValueChange = { apellido = it },
                label = { Text("Apellido") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre de usuario") },
            supportingText = { Text("No necesitas correo ni número de teléfono.") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            supportingText = { Text("Mínimo 6 caracteres.") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (registerMode) {
            OutlinedTextField(
                value = confirmation,
                onValueChange = { confirmation = it },
                label = { Text("Confirmar contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Button(
            onClick = {
                if (registerMode) {
                    controlador.registrar(username, nombre, apellido, password, confirmation)
                } else {
                    controlador.iniciarSesion(username, password)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = !controlador.cargando
        ) {
            if (controlador.cargando) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text(if (registerMode) "Crear cuenta" else "Entrar")
            }
        }
        OutlinedButton(
            onClick = controlador::entrarComoInvitado,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Continuar como anónimo")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaInicio(controlador: ControladorAsistenTed, navController: NavHostController) {
    var query by remember { mutableStateOf("") }
    var selectedInstitution by remember { mutableStateOf("Todas") }
    val institutions = listOf("Todas") + controlador.tramites.map { it.institution }.distinct()
    val filtered = controlador.tramites.filter {
        (selectedInstitution == "Todas" || it.institution == selectedInstitution) &&
            (query.isBlank() || it.title.contains(query, ignoreCase = true) || it.institution.contains(query, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hola, ${controlador.usuarioActual?.nombreVisible ?: "Anónimo"}", fontWeight = FontWeight.Bold)
                        Text("Elige un trámite para guiarte paso a paso.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Buscar trámite") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    supportingText = { Text("Puedes buscar por nombre o institución.") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    institutions.take(3).forEach { institution ->
                        FilterChip(
                            selected = selectedInstitution == institution,
                            onClick = { selectedInstitution = institution },
                            label = { Text(institution, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                    }
                }
            }
            items(filtered, key = { it.id }) { procedure ->
                TarjetaTramite(
                    procedure = procedure,
                    isFavorite = controlador.favoritos.contains(procedure.id),
                    onFavorite = { controlador.alternarFavorito(procedure.id) },
                    onOpen = {
                        controlador.marcarConsultado(procedure.id)
                        navController.navigate(Rutas.detail(procedure.id))
                    },
                    textoGrande = controlador.configuracionAccesibilidad.textoGrande
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaDetalleTramite(
    controlador: ControladorAsistenTed,
    procedure: Tramite,
    navController: NavHostController
) {
    val context = LocalContext.current
    val speaker = recordarLectorGuia()
    var commentText by remember { mutableStateOf("") }
    val completed = controlador.pasosCompletados[procedure.id].orEmpty()
    val fullText = buildString {
        append(procedure.title).append(". ")
        procedure.steps.forEach { step ->
            append(step.title).append(". ")
            append(step.description).append(". ")
            append(step.textoAyuda).append(". ")
        }
    }

    LaunchedEffect(procedure.id) {
        controlador.cargarComentarios(procedure.id)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(procedure.institution) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { controlador.alternarFavorito(procedure.id) }) {
                        Icon(
                            if (controlador.favoritos.contains(procedure.id)) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Favorito"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(procedure.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(procedure.summary, style = cuerpoLegible(controlador))
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(procedure.urlOficial)))
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Portal oficial")
                    }
                    OutlinedButton(
                        onClick = {
                            if (speaker.isSpeaking) speaker.detener() else speaker.leer(fullText)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(if (speaker.isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (speaker.isSpeaking) "Detener" else "Escuchar")
                    }
                }
            }
            items(procedure.steps, key = { it.id }) { step ->
                TarjetaPasoGuia(
                    title = step.title,
                    description = step.description,
                    textoAyuda = step.textoAyuda,
                    espacioImagen = step.espacioImagen,
                    elementosRevision = step.elementosRevision,
                    completed = completed.contains(step.id),
                    onCompletedChange = { controlador.alternarPaso(procedure.id, step.id) },
                    textoGrande = controlador.configuracionAccesibilidad.textoGrande
                )
            }
            item {
                SeccionForo(
                    controlador = controlador,
                    procedure = procedure,
                    commentText = commentText,
                    onCommentTextChange = { commentText = it },
                    onPublish = {
                        controlador.agregarComentario(procedure.id, commentText)
                        commentText = ""
                    }
                )
            }
        }
    }
}

@Composable
private fun PantallaListaTramites(
    title: String,
    emptyText: String,
    tramites: List<Tramite>,
    controlador: ControladorAsistenTed,
    navController: NavHostController
) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (tramites.isEmpty()) {
            TarjetaAviso(title = "Sin datos", text = emptyText)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(tramites, key = { it.id }) { procedure ->
                    TarjetaTramite(
                        procedure = procedure,
                        isFavorite = controlador.favoritos.contains(procedure.id),
                        onFavorite = { controlador.alternarFavorito(procedure.id) },
                        onOpen = {
                            controlador.marcarConsultado(procedure.id)
                            navController.navigate(Rutas.detail(procedure.id))
                        },
                        textoGrande = controlador.configuracionAccesibilidad.textoGrande
                    )
                }
            }
        }
    }
}

@Composable
private fun PantallaRecordatorios(controlador: ControladorAsistenTed) {
    val context = LocalContext.current
    var selectedTramiteId by remember { mutableStateOf(controlador.tramites.first().id) }
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var time by remember { mutableStateOf("09:00") }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Recordatorios", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Agenda una fecha para volver a revisar un trámite.", style = cuerpoLegible(controlador))
        }
        if (controlador.usuarioActual?.esInvitado == true) {
            item {
                TarjetaAviso("Cuenta necesaria", "Para guardar recordatorios y recibir avisos necesitas iniciar sesión.")
            }
        } else {
            item {
                Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Nuevo recordatorio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        SelectorTramite(controlador.tramites, selectedTramiteId) { selectedTramiteId = it }
                        OutlinedTextField(title, { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(notes, { notes = it }, label = { Text("Nota de ayuda") }, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(date, { date = it }, label = { Text("Fecha") }, supportingText = { Text("AAAA-MM-DD") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(time, { time = it }, label = { Text("Hora") }, supportingText = { Text("HH:MM") }, modifier = Modifier.weight(1f))
                        }
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                val millis = analizarFechaHora(date, time)
                                if (millis == null) {
                                    controlador.mostrarMensaje("Usa una fecha y hora válidas.")
                                } else {
                                    controlador.agregarRecordatorio(selectedTramiteId, title, notes, millis)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Guardar recordatorio")
                        }
                    }
                }
            }
        }
        items(controlador.recordatorios, key = { it.id }) { reminder ->
            TarjetaRecordatorio(reminder, controlador)
        }
    }
}

@Composable
private fun PantallaPerfil(controlador: ControladorAsistenTed) {
    val user = controlador.usuarioActual
    var nombre by remember(user?.uid) { mutableStateOf(user?.nombre.orEmpty()) }
    var apellido by remember(user?.uid) { mutableStateOf(user?.apellido.orEmpty()) }
    var selectedAvatarId by remember(user?.uid) { mutableStateOf(user?.avatarId ?: AvataresPerfil.defaultId) }
    val selectedAvatar = AvataresPerfil.find(selectedAvatarId)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Perfil", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (user?.esInvitado == true) {
            TarjetaAviso("Estás como anónimo", "Puedes consultar guías. Para guardar perfil, favoritos, historial y foro, crea una cuenta.")
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            VistaAvatar(avatar = selectedAvatar, size = 76)
            Column {
                Text(user?.nombreVisible.orEmpty(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Avatar preestablecido: ${selectedAvatar.label}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        OutlinedTextField(value = user?.username.orEmpty(), onValueChange = {}, label = { Text("Usuario") }, enabled = false, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(apellido, { apellido = it }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth())
        SelectorAvatar(
            selectedAvatarId = selectedAvatarId,
            onSelected = { selectedAvatarId = it }
        )
        TarjetaAviso("Foto de perfil", "Elige una imagen preestablecida. La app no sube fotos personales ni usa almacenamiento en la nube.")
        Button(
            onClick = { controlador.actualizarPerfil(nombre, apellido, selectedAvatarId) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Guardar cambios")
        }
        OutlinedButton(
            onClick = controlador::cerrarSesion,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Cerrar sesión")
        }
    }
}

@Composable
private fun SelectorAvatar(
    selectedAvatarId: String,
    onSelected: (String) -> Unit
) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Selecciona tu avatar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Son imágenes locales de la app. No se sube ningún archivo personal.",
                style = MaterialTheme.typography.bodyMedium
            )
            AvataresPerfil.options.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { avatar ->
                        TarjetaOpcionAvatar(
                            avatar = avatar,
                            selected = avatar.id == selectedAvatarId,
                            onClick = { onSelected(avatar.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaOpcionAvatar(
    avatar: AvatarPerfil,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VistaAvatar(avatar = avatar, size = 52)
            Text(avatar.label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

@Composable
private fun VistaAvatar(avatar: AvatarPerfil, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(avatar.background, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            avatar.initials,
            color = avatar.foreground,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class AvatarPerfil(
    val id: String,
    val label: String,
    val initials: String,
    val background: Color,
    val foreground: Color
)

private object AvataresPerfil {
    const val defaultId = "azul"

    val options = listOf(
        AvatarPerfil("azul", "Azul", "AZ", Color(0xFFDDEBFF), Color(0xFF204D85)),
        AvatarPerfil("amarillo", "Amarillo", "AM", Color(0xFFFFF4C2), Color(0xFF6E5200)),
        AvatarPerfil("rojo", "Rojo", "RJ", Color(0xFFFFE1E1), Color(0xFF8B2E2E)),
        AvatarPerfil("verde", "Verde", "VE", Color(0xFFDFF5E3), Color(0xFF236336)),
        AvatarPerfil("celeste", "Celeste", "CE", Color(0xFFD8F3FF), Color(0xFF1E5C70)),
        AvatarPerfil("violeta", "Violeta", "VI", Color(0xFFEDE4FF), Color(0xFF59408C))
    )

    fun find(id: String): AvatarPerfil = options.firstOrNull { it.id == id } ?: options.first()
}

@Composable
private fun PantallaAccesibilidad(controlador: ControladorAsistenTed) {
    val configuracion = controlador.configuracionAccesibilidad
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Accesibilidad", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Ajusta la lectura visual de toda la app.", style = cuerpoLegible(controlador))
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
private fun TarjetaTramite(
    procedure: Tramite,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onOpen: () -> Unit,
    textoGrande: Boolean
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(procedure.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(procedure.institution, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onFavorite) {
                    Icon(if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = "Favorito")
                }
            }
            Text(procedure.summary, style = if (textoGrande) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(procedure.category) })
                AssistChip(onClick = {}, label = { Text("${procedure.steps.size} pasos") })
            }
            Button(onClick = onOpen, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(8.dp)) {
                Text("Ver guía")
            }
        }
    }
}

@Composable
private fun TarjetaPasoGuia(
    title: String,
    description: String,
    textoAyuda: String,
    espacioImagen: String,
    elementosRevision: List<String>,
    completed: Boolean,
    onCompletedChange: () -> Unit,
    textoGrande: Boolean
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = completed, onCheckedChange = { onCompletedChange() })
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }
            Text(description, style = if (textoGrande) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge)
            TarjetaAviso("Ayuda", textoAyuda)
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(espacioImagen, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
            }
            elementosRevision.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(item, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun SeccionForo(
    controlador: ControladorAsistenTed,
    procedure: Tramite,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onPublish: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Forum, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Foro del trámite", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        if (controlador.usuarioActual?.esInvitado == true) {
            TarjetaAviso("Solo lectura", "Inicia sesión para publicar preguntas o comentarios.")
        } else {
            OutlinedTextField(
                value = commentText,
                onValueChange = onCommentTextChange,
                label = { Text("Pregunta o comentario") },
                supportingText = { Text("Escribe con respeto y evita datos personales sensibles.") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Button(onClick = onPublish, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(8.dp)) {
                Text("Publicar")
            }
        }
        val comentarios = controlador.comentarios[procedure.id].orEmpty()
        if (comentarios.isEmpty()) {
            Text("Aún no hay comentarios para este trámite.", style = MaterialTheme.typography.bodyMedium)
        } else {
            comentarios.forEach { comment ->
                Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(comment.username, fontWeight = FontWeight.Bold)
                        Text(comment.text)
                        Text(formatearFecha(comment.createdAtMillis), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectorTramite(tramites: List<Tramite>, selectedId: String, onSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Trámite relacionado", style = MaterialTheme.typography.labelLarge)
        tramites.take(6).forEach { procedure ->
            FilterChip(
                selected = selectedId == procedure.id,
                onClick = { onSelected(procedure.id) },
                label = { Text(procedure.title) }
            )
        }
    }
}

@Composable
private fun TarjetaRecordatorio(reminder: Recordatorio, controlador: ControladorAsistenTed) {
    val procedure = CatalogoTramites.buscarTramite(reminder.tramiteId)
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(reminder.title, fontWeight = FontWeight.Bold)
                Text(procedure?.title ?: "Trámite", style = MaterialTheme.typography.bodyMedium)
                Text(formatearFecha(reminder.programadoEnMillis), style = MaterialTheme.typography.labelMedium)
                if (reminder.notes.isNotBlank()) Text(reminder.notes, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { controlador.borrarRecordatorio(reminder) }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}

@Composable
private fun FilaConfiguracion(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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

@Composable
private fun TarjetaAviso(title: String, text: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun BarraInferiorPrincipal(navController: NavHostController) {
    val entry by navController.currentBackStackEntryAsState()
    val current = entry?.destination?.route.orEmpty()
    val items = listOf(
        ElementoBarraInferior(Rutas.HOME, "Inicio", Icons.Default.Home),
        ElementoBarraInferior(Rutas.FAVORITES, "Favoritos", Icons.Default.Bookmark),
        ElementoBarraInferior(Rutas.HISTORY, "Historial", Icons.Default.History),
        ElementoBarraInferior(Rutas.REMINDERS, "Avisos", Icons.Default.Notifications),
        ElementoBarraInferior(Rutas.PROFILE, "Perfil", Icons.Default.AccountCircle),
        ElementoBarraInferior(Rutas.ACCESSIBILITY, "Acceso", Icons.Default.Accessibility)
    )
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = current == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Rutas.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, maxLines = 1) }
            )
        }
    }
}

private data class ElementoBarraInferior(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private object Rutas {
    const val HOME = "home"
    const val FAVORITES = "favoritos"
    const val HISTORY = "historial"
    const val REMINDERS = "recordatorios"
    const val PROFILE = "perfil"
    const val ACCESSIBILITY = "accessibility"
    const val DETAIL = "procedure/{tramiteId}"

    fun detail(tramiteId: String) = "procedure/$tramiteId"
}

@Composable
private fun cuerpoLegible(controlador: ControladorAsistenTed) =
    if (controlador.configuracionAccesibilidad.textoGrande) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge

@Composable
private fun recordarLectorGuia(): LectorGuia {
    val context = LocalContext.current
    val speaker = remember { LectorGuia(context.applicationContext) }
    DisposableEffect(Unit) {
        onDispose { speaker.cerrar() }
    }
    return speaker
}

class LectorGuia(context: Context) : TextToSpeech.OnInitListener {
    private val textToSpeech = TextToSpeech(context, this)
    private var ready by mutableStateOf(false)
    var isSpeaking by mutableStateOf(false)
        private set

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            textToSpeech.language = Locale.Builder().setLanguage("es").setRegion("EC").build()
            textToSpeech.setSpeechRate(0.88f)
        }
    }

    fun leer(text: String) {
        if (!ready) return
        isSpeaking = true
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "guide")
    }

    fun detener() {
        textToSpeech.stop()
        isSpeaking = false
    }

    fun cerrar() {
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}

private fun analizarFechaHora(date: String, time: String): Long? {
    return try {
        LocalDateTime.parse("$date $time", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun formatearFecha(millis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}


