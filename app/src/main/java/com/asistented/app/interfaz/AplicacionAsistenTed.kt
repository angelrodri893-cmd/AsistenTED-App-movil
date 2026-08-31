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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.asistented.app.R
import com.asistented.app.interfaz.tema.TemaAsistenTED
import com.asistented.app.dominio.ReglasAutenticacion
import com.asistented.app.presentacion.ControladorAsistenTed
import com.asistented.app.presentacion.EstadoCatalogo
import com.asistented.app.presentacion.esUsuarioDuplicadoEnAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
private fun Modifier.ocultarTecladoAlTocarFuera(): Modifier {
    val focusManager = LocalFocusManager.current
    return pointerInput(Unit) {
        detectTapGestures(onTap = { focusManager.clearFocus() })
    }
}

@Composable
private fun CampoEntrada(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    ayuda: String? = null,
    habilitado: Boolean = true,
    soloLectura: Boolean = false,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    accionIme: ImeAction = ImeAction.Done,
    transformacionVisual: VisualTransformation = VisualTransformation.None,
    iconoInicial: (@Composable () -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = valor,
        onValueChange = { nuevoValor ->
            alCambiar(nuevoValor.replace("\n", " "))
        },
        label = { Text(etiqueta) },
        supportingText = ayuda?.let { texto -> { Text(texto) } },
        leadingIcon = iconoInicial,
        enabled = habilitado,
        readOnly = soloLectura,
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado,
            imeAction = accionIme
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() },
            onNext = { focusManager.clearFocus() },
            onSearch = { focusManager.clearFocus() }
        ),
        visualTransformation = transformacionVisual,
        modifier = modifier
    )
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
            PantallaAutenticacionDisenada(
                controlador = controlador,
                modifier = Modifier.padding(padding)
            )
        }
        return
    }

    val avatarIdActual = controlador.usuarioActual?.avatarId.orEmpty()

    when (controlador.estadoCatalogo) {
        EstadoCatalogo.CARGANDO -> {
            PantallaCargandoCatalogo(modifier = Modifier.statusBarsPadding())
            return
        }
        EstadoCatalogo.ERROR_CONEXION -> {
            PantallaErrorConexion(
                onReintentar = controlador::actualizarCatalogoGobEc,
                modifier = Modifier.statusBarsPadding()
            )
            return
        }
        EstadoCatalogo.DISPONIBLE -> Unit
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BarraInferiorPrincipal(navController, avatarIdActual) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.HOME,
            modifier = Modifier
                .padding(padding)
                .ocultarTecladoAlTocarFuera()
        ) {
            composable(Rutas.HOME) {
                val tramitesActuales = controlador.tramites.toList()
                PantallaPrincipal(
                    nombreUsuario = controlador.usuarioActual?.nombreVisible.orEmpty(),
                    avatarId = avatarIdActual,
                    tramites = tramitesActuales,
                    favoritos = controlador.favoritos.toSet(),
                    mostrarAvisoInicial = controlador.mostrarAyudaPrincipal,
                    onAbrirTramite = { tramite ->
                        controlador.marcarConsultado(tramite.id)
                        navController.navigate(Rutas.detail(tramite.id))
                    },
                    onAlternarFavorito = { tramite ->
                        controlador.alternarFavorito(tramite.id)
                    },
                    onAbrirPerfil = {
                        navController.navigate(Rutas.PROFILE) {
                            popUpTo(Rutas.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onDescartarAviso = controlador::descartarAyudaPrincipal,
                )
            }
            composable(
                route = Rutas.DETAIL,
                arguments = listOf(navArgument("tramiteId") { type = NavType.StringType })
            ) { entry ->
                val tramiteId = entry.arguments?.getString("tramiteId").orEmpty()
                controlador.buscarTramite(tramiteId)?.let { tramite ->
                    val contexto = LocalContext.current
                    val lector = recordarLectorGuia()
                    val textoGuia = remember(tramite.id) { construirTextoGuia(tramite) }

                    LaunchedEffect(tramite.id) { controlador.cargarComentarios(tramite.id) }

                    PantallaDetalleTramiteRedisenada(
                        tramite = tramite,
                        pasosCompletados = controlador.pasosCompletados[tramite.id].orEmpty(),
                        comentarios = controlador.comentarios[tramite.id].orEmpty(),
                        usuarioActualId = controlador.usuarioActual?.uid,
                        avatarId = avatarIdActual,
                        puedeParticipar = controlador.usuarioActual?.esInvitado != true,
                        mostrarAvisoInicial = controlador.mostrarAyudaDetalle,
                        estaLeyendo = lector.isSpeaking,
                        onRegresar = { navController.popBackStack() },
                        onAbrirPerfil = {
                            navController.navigate(Rutas.PROFILE) {
                                popUpTo(Rutas.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onAbrirPortal = {
                            contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(tramite.urlTramiteEnLinea ?: tramite.urlOficial)))
                        },
                        onAlternarLectura = {
                            if (lector.isSpeaking) lector.detener() else lector.leer(textoGuia)
                        },
                        onAlternarPaso = { paso -> controlador.alternarPaso(tramite.id, paso.id) },
                        onPublicarComentario = { texto -> controlador.agregarComentario(tramite.id, texto) },
                        onEditarComentario = controlador::editarComentario,
                        onEliminarComentario = controlador::eliminarComentario,
                        onResponderComentario = controlador::responderComentario,
                        onDescartarAviso = controlador::descartarAyudaDetalle
                    )
                }
            }
            composable(Rutas.FAVORITES) {
                PantallaFavoritos(
                    tramites = controlador.tramites.toList(),
                    favoritos = controlador.favoritos.toSet(),
                    avatarId = avatarIdActual,
                    mostrarAvisoInicial = controlador.mostrarAyudaFavoritos,
                    onRegresar = { navController.popBackStack() },
                    onAbrirPerfil = {
                        navController.navigate(Rutas.PROFILE) {
                            popUpTo(Rutas.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAbrirTramite = { tramite ->
                        controlador.marcarConsultado(tramite.id)
                        navController.navigate(Rutas.detail(tramite.id))
                    },
                    onAlternarFavorito = { tramite ->
                        controlador.alternarFavorito(tramite.id)
                    },
                    onDescartarAviso = controlador::descartarAyudaFavoritos
                )
            }
            composable(Rutas.HISTORY) {
                PantallaHistorialRedisenada(
                    tramites = controlador.tramites.toList(),
                    idsHistorial = controlador.historial.map { it.tramiteId },
                    favoritos = controlador.favoritos.toSet(),
                    avatarId = avatarIdActual,
                    onRegresar = { navController.popBackStack() },
                    onAbrirPerfil = { navController.popBackStack(Rutas.PROFILE, inclusive = false) },
                    onAbrirTramite = { tramite ->
                        controlador.marcarConsultado(tramite.id)
                        navController.navigate(Rutas.detail(tramite.id))
                    },
                    onAlternarFavorito = { tramite -> controlador.alternarFavorito(tramite.id) }
                )
            }
            composable(Rutas.REMINDERS) {
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
                PantallaNotificaciones(
                    tramites = controlador.tramites.toList(),
                    recordatorios = controlador.recordatorios.toList(),
                    avatarId = avatarIdActual,
                    esInvitado = controlador.usuarioActual?.esInvitado == true,
                    mostrarAvisoInicial = controlador.mostrarAyudaNotificaciones,
                    onRegresar = { navController.popBackStack() },
                    onAbrirPerfil = {
                        navController.navigate(Rutas.PROFILE) {
                            popUpTo(Rutas.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onGuardarRecordatorio = { tramiteId, titulo, nota, programadoEnMillis ->
                        controlador.agregarRecordatorio(
                            tramiteId = tramiteId,
                            title = titulo,
                            notes = nota,
                            programadoEnMillis = programadoEnMillis
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onBorrarRecordatorio = controlador::borrarRecordatorio,
                    onDescartarAviso = controlador::descartarAyudaNotificaciones
                )
            }
            composable(Rutas.PROFILE) {
                PantallaPerfilRedisenada(
                    perfil = controlador.usuarioActual,
                    configuracion = controlador.configuracionAccesibilidad,
                    mostrarAvisoInicial = controlador.mostrarAyudaPerfil,
                    onRegresar = { navController.popBackStack() },
                    onGuardarPerfil = { nombre, apellido, avatarId, onResultado ->
                        controlador.actualizarPerfil(nombre, apellido, avatarId, onResultado)
                    },
                    onActualizarAccesibilidad = controlador::actualizarAccesibilidad,
                    onAbrirHistorial = { navController.navigate(Rutas.HISTORY) },
                    onCerrarSesion = controlador::cerrarSesion,
                    onDescartarAviso = controlador::descartarAyudaPerfil
                )
            }
            composable(Rutas.ACCESSIBILITY) {
                PantallaAccesibilidad(controlador)
            }
        }
    }
}

private enum class ModoAutenticacion {
    InicioSesion,
    Registro
}

@Composable
private fun PantallaAutenticacionDisenada(controlador: ControladorAsistenTed, modifier: Modifier = Modifier) {
    var mostrarBienvenida by remember { mutableStateOf(true) }
    var modo by remember { mutableStateOf(ModoAutenticacion.InicioSesion) }
    var username by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var mostrarAvisoAnonimo by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmationVisible by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var nombreError by remember { mutableStateOf<String?>(null) }
    var apellidoError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmationError by remember { mutableStateOf<String?>(null) }
    var loginError by remember { mutableStateOf<String?>(null) }

    val errorUsuarioVacio = stringResource(R.string.auth_error_usuario_vacio)
    val errorUsuarioInvalido = stringResource(R.string.auth_error_usuario_invalido)
    val errorUsuarioRepetido = stringResource(R.string.auth_error_usuario_repetido)
    val errorNombreVacio = stringResource(R.string.auth_error_nombre_vacio)
    val errorApellidoVacio = stringResource(R.string.auth_error_apellido_vacio)
    val errorPasswordVacia = stringResource(R.string.auth_error_password_vacia)
    val errorPasswordCorta = stringResource(R.string.auth_error_password_corta)
    val errorConfirmacionVacia = stringResource(R.string.auth_error_confirmacion_vacia)
    val errorConfirmacionDiferente = stringResource(R.string.auth_error_confirmacion_diferente)
    val errorLogin = stringResource(R.string.auth_error_login)
    val errorRegistro = stringResource(R.string.auth_error_registro)

    LaunchedEffect(Unit) {
        delay(1400)
        mostrarBienvenida = false
    }

    fun validarUsuarioLocal(): String? {
        val normalized = ReglasAutenticacion.normalizarUsuario(username)
        return when {
            normalized.isBlank() -> errorUsuarioVacio
            ReglasAutenticacion.validarUsuario(username) != null -> errorUsuarioInvalido
            else -> null
        }
    }

    fun validarPasswordLocal(): String? {
        return when {
            password.isBlank() -> errorPasswordVacia
            password.length < 6 -> errorPasswordCorta
            else -> null
        }
    }

    fun limpiarErroresDeFormulario() {
        usernameError = null
        nombreError = null
        apellidoError = null
        passwordError = null
        confirmationError = null
        loginError = null
    }

    fun enviarInicioSesion() {
        usernameError = validarUsuarioLocal()
        passwordError = validarPasswordLocal()
        confirmationError = null
        loginError = null
        if (usernameError != null || passwordError != null) return

        controlador.iniciarSesion(username, password) {
            loginError = errorLogin
        }
    }

    fun enviarRegistro() {
        usernameError = validarUsuarioLocal()
        nombreError = if (nombre.isBlank()) errorNombreVacio else null
        apellidoError = if (apellido.isBlank()) errorApellidoVacio else null
        passwordError = validarPasswordLocal()
        confirmationError = when {
            confirmation.isBlank() -> errorConfirmacionVacia
            confirmation != password -> errorConfirmacionDiferente
            else -> null
        }
        loginError = null
        if (
            usernameError != null ||
            nombreError != null ||
            apellidoError != null ||
            passwordError != null ||
            confirmationError != null
        ) return

        fun registrarConFirebaseAuth() {
            controlador.registrar(username, nombre, apellido, password, confirmation) { error ->
                usernameError = if (error.esUsuarioDuplicadoEnAuth()) {
                    errorUsuarioRepetido
                } else {
                    errorRegistro
                }
            }
        }

        // Firebase Auth mantiene la unicidad real del usuario convertido a correo interno.
        // La consulta previa a Firestore no es necesaria y no debe exponer documentos de usuarios.
        registrarConFirebaseAuth()
    }

    if (mostrarBienvenida) {
        PantallaBienvenidaAsistenTED(modifier = modifier)
        return
    }

    val cargando = controlador.cargando
    Box(modifier = modifier.fillMaxSize()) {
        PantallaAutenticacionContenido(
            modo = modo,
            username = username,
            nombre = nombre,
            apellido = apellido,
            password = password,
            confirmation = confirmation,
            usernameError = usernameError,
            nombreError = nombreError,
            apellidoError = apellidoError,
            passwordError = passwordError,
            confirmationError = confirmationError,
            loginError = loginError,
            cargando = cargando,
            passwordVisible = passwordVisible,
            confirmationVisible = confirmationVisible,
            onModoChange = {
                modo = it
                limpiarErroresDeFormulario()
            },
            onUsernameChange = {
                username = it
                usernameError = null
                loginError = null
            },
            onNombreChange = {
                nombre = it
                nombreError = null
            },
            onApellidoChange = {
                apellido = it
                apellidoError = null
            },
            onPasswordChange = {
                password = it
                passwordError = null
                confirmationError = null
                loginError = null
            },
            onConfirmationChange = {
                confirmation = it
                confirmationError = null
            },
            onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
            onConfirmationVisibilityChange = { confirmationVisible = !confirmationVisible },
            onPrimaryClick = {
                if (modo == ModoAutenticacion.Registro) enviarRegistro() else enviarInicioSesion()
            },
            onAnonymousClick = { mostrarAvisoAnonimo = true },
            modifier = Modifier.blur(if (mostrarAvisoAnonimo) 4.dp else 0.dp)
        )

        if (mostrarAvisoAnonimo) {
            AvisoIngresoAnonimo(
                onContinuar = {
                    mostrarAvisoAnonimo = false
                    controlador.entrarComoInvitado()
                },
                onVolver = { mostrarAvisoAnonimo = false },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun PantallaAutenticacionContenido(
    modo: ModoAutenticacion,
    username: String,
    nombre: String,
    apellido: String,
    password: String,
    confirmation: String,
    usernameError: String?,
    nombreError: String?,
    apellidoError: String?,
    passwordError: String?,
    confirmationError: String?,
    loginError: String?,
    cargando: Boolean,
    passwordVisible: Boolean,
    confirmationVisible: Boolean,
    onModoChange: (ModoAutenticacion) -> Unit,
    onUsernameChange: (String) -> Unit,
    onNombreChange: (String) -> Unit,
    onApellidoChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmationChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onConfirmationVisibilityChange: () -> Unit,
    onPrimaryClick: () -> Unit,
    onAnonymousClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val compact = maxHeight < 720.dp
        val margenBase = if (maxWidth < 360.dp) 24.dp else 38.dp
        val horizontalPadding = if (maxWidth > 560.dp) (maxWidth - 480.dp) / 2 else margenBase
        val logoWidth = if (compact) 220.dp else 248.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = if (compact) 24.dp else 72.dp,
                    bottom = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.logo_principal),
                contentDescription = stringResource(R.string.cd_logo_tramite_ecuador),
                modifier = Modifier
                    .widthIn(max = logoWidth)
                    .fillMaxWidth()
                    .height(if (compact) 92.dp else 116.dp)
            )

            Spacer(Modifier.height(if (compact) 8.dp else 22.dp))

            SelectorModoAutenticacion(
                modo = modo,
                onModoChange = onModoChange,
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth()
            )

            Spacer(Modifier.height(if (compact) 6.dp else 10.dp))

            if (modo == ModoAutenticacion.Registro) {
                CampoAutenticacion(
                    valor = nombre,
                    alCambiar = onNombreChange,
                    placeholder = stringResource(R.string.auth_nombre),
                    contentDescriptionIcono = stringResource(R.string.cd_icono_nombre),
                    icono = Icons.Default.Person,
                    error = nombreError,
                    accionIme = ImeAction.Next,
                    modifier = Modifier.fillMaxWidth()
                )
                CampoAutenticacion(
                    valor = apellido,
                    alCambiar = onApellidoChange,
                    placeholder = stringResource(R.string.auth_apellido),
                    contentDescriptionIcono = stringResource(R.string.cd_icono_apellido),
                    icono = Icons.Default.Person,
                    error = apellidoError,
                    accionIme = ImeAction.Next,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            CampoAutenticacion(
                valor = username,
                alCambiar = onUsernameChange,
                placeholder = stringResource(R.string.auth_usuario),
                contentDescriptionIcono = stringResource(R.string.cd_icono_usuario),
                icono = Icons.Default.Person,
                error = usernameError,
                accionIme = ImeAction.Next,
                modifier = Modifier.fillMaxWidth()
            )

            CampoPasswordAutenticacion(
                valor = password,
                alCambiar = onPasswordChange,
                placeholder = stringResource(R.string.auth_password),
                visible = passwordVisible,
                alAlternarVisibilidad = onPasswordVisibilityChange,
                error = passwordError,
                accionIme = if (modo == ModoAutenticacion.Registro) ImeAction.Next else ImeAction.Done,
                modifier = Modifier.fillMaxWidth()
            )

            if (modo == ModoAutenticacion.Registro) {
                CampoPasswordAutenticacion(
                    valor = confirmation,
                    alCambiar = onConfirmationChange,
                    placeholder = stringResource(R.string.auth_repetir_password),
                    visible = confirmationVisible,
                    alAlternarVisibilidad = onConfirmationVisibilityChange,
                    error = confirmationError,
                    accionIme = ImeAction.Done,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (loginError != null) {
                Text(
                    text = loginError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                )
            }

            Spacer(Modifier.height(if (compact) 4.dp else 8.dp))

            BotonAccionAutenticacion(
                texto = if (modo == ModoAutenticacion.Registro) {
                    stringResource(R.string.auth_registrarse)
                } else {
                    stringResource(R.string.auth_iniciar_sesion)
                },
                cargando = cargando,
                onClick = onPrimaryClick,
                modifier = Modifier.fillMaxWidth()
            )

            if (modo == ModoAutenticacion.InicioSesion) {
                BotonAnonimoAutenticacion(
                    onClick = onAnonymousClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SelectorModoAutenticacion(
    modo: ModoAutenticacion,
    onModoChange: (ModoAutenticacion) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BotonModoAutenticacion(
            texto = stringResource(R.string.auth_iniciar_sesion),
            seleccionado = modo == ModoAutenticacion.InicioSesion,
            onClick = { onModoChange(ModoAutenticacion.InicioSesion) },
            modifier = Modifier.weight(1f)
        )
        BotonModoAutenticacion(
            texto = stringResource(R.string.auth_registrarse),
            seleccionado = modo == ModoAutenticacion.Registro,
            onClick = { onModoChange(ModoAutenticacion.Registro) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BotonModoAutenticacion(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(DimensionesDiseno.altoAccion),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (seleccionado) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (seleccionado) 5.dp else 0.dp,
            pressedElevation = 1.dp
        ),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Text(text = texto, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun CampoAutenticacion(
    valor: String,
    alCambiar: (String) -> Unit,
    placeholder: String,
    contentDescriptionIcono: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    accionIme: ImeAction = ImeAction.Done,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    transformacionVisual: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = valor,
            onValueChange = { alCambiar(it.replace("\n", " ")) },
            modifier = Modifier
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = icono,
                    contentDescription = contentDescriptionIcono,
                    tint = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = trailingIcon,
            singleLine = true,
            isError = error != null,
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions = KeyboardOptions(
                keyboardType = tipoTeclado,
                imeAction = accionIme
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() },
                onNext = { focusManager.clearFocus() }
            ),
            visualTransformation = transformacionVisual,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                errorTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 18.dp)
            )
        }
    }
}

@Composable
private fun CampoPasswordAutenticacion(
    valor: String,
    alCambiar: (String) -> Unit,
    placeholder: String,
    visible: Boolean,
    alAlternarVisibilidad: () -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
    accionIme: ImeAction = ImeAction.Done
) {
    CampoAutenticacion(
        valor = valor,
        alCambiar = alCambiar,
        placeholder = placeholder,
        contentDescriptionIcono = stringResource(R.string.cd_icono_password),
        icono = Icons.Default.Lock,
        error = error,
        accionIme = accionIme,
        tipoTeclado = KeyboardType.Password,
        transformacionVisual = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = alAlternarVisibilidad) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (visible) {
                        stringResource(R.string.cd_ocultar_password)
                    } else {
                        stringResource(R.string.cd_mostrar_password)
                    },
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        modifier = modifier
    )
}

@Composable
private fun BotonAccionAutenticacion(
    texto: String,
    cargando: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !cargando,
        modifier = modifier.height(DimensionesDiseno.altoAccion),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.72f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSecondary
            )
        } else {
            Text(text = texto, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BotonAnonimoAutenticacion(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(DimensionesDiseno.altoAccion),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
    ) {
        Text(
            text = stringResource(R.string.auth_ingresar_anonimo),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AvisoIngresoAnonimo(
    onContinuar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.36f))
    )
    Card(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = stringResource(R.string.cd_aviso_anonimo),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = stringResource(R.string.auth_aviso_anonimo),
                color = MaterialTheme.colorScheme.onSecondary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onContinuar,
                    modifier = Modifier.height(DimensionesDiseno.altoAccion),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(text = stringResource(R.string.auth_continuar), style = MaterialTheme.typography.labelMedium)
                }
                Button(
                    onClick = onVolver,
                    modifier = Modifier.height(DimensionesDiseno.altoAccion),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(text = stringResource(R.string.auth_volver), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun PantallaBienvenidaAsistenTED(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.logo_individual_multicolor),
                contentDescription = stringResource(R.string.cd_logo_asistented),
                modifier = Modifier.size(132.dp)
            )
            Text(
                text = stringResource(R.string.auth_bienvenida),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        OndasBienvenida(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun OndasBienvenida(modifier: Modifier = Modifier) {
    val amarillo = MaterialTheme.colorScheme.secondary
    val azul = MaterialTheme.colorScheme.primary
    val rojo = MaterialTheme.colorScheme.tertiary
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(255.dp)
    ) {
        val yellow = Path().apply {
            moveTo(0f, size.height * 0.63f)
            cubicTo(size.width * 0.30f, size.height * 0.52f, size.width * 0.48f, size.height * 0.80f, size.width * 0.75f, size.height * 0.25f)
            cubicTo(size.width * 0.88f, 0f, size.width, size.height * 0.03f, size.width, size.height * 0.03f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        val blue = Path().apply {
            moveTo(0f, size.height * 0.88f)
            cubicTo(size.width * 0.24f, size.height * 0.72f, size.width * 0.47f, size.height * 0.80f, size.width * 0.70f, size.height * 0.55f)
            cubicTo(size.width * 0.86f, size.height * 0.35f, size.width * 0.93f, size.height * 0.30f, size.width, size.height * 0.32f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        val red = Path().apply {
            moveTo(0f, size.height)
            cubicTo(size.width * 0.18f, size.height * 0.95f, size.width * 0.38f, size.height * 0.88f, size.width * 0.56f, size.height * 0.78f)
            cubicTo(size.width * 0.74f, size.height * 0.68f, size.width * 0.86f, size.height * 0.50f, size.width, size.height * 0.49f)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(yellow, amarillo.copy(alpha = 0.74f))
        drawPath(blue, azul)
        drawPath(red, rojo)
    }
}

@Preview(name = "Login AsistenTED", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewPantallaInicioSesion() {
    TemaAsistenTED(darkTheme = false) {
        PantallaAutenticacionContenido(
            modo = ModoAutenticacion.InicioSesion,
            username = "Alex123",
            nombre = "",
            apellido = "",
            password = "rodriguez123",
            confirmation = "",
            usernameError = null,
            nombreError = null,
            apellidoError = null,
            passwordError = null,
            confirmationError = null,
            loginError = null,
            cargando = false,
            passwordVisible = false,
            confirmationVisible = false,
            onModoChange = {},
            onUsernameChange = {},
            onNombreChange = {},
            onApellidoChange = {},
            onPasswordChange = {},
            onConfirmationChange = {},
            onPasswordVisibilityChange = {},
            onConfirmationVisibilityChange = {},
            onPrimaryClick = {},
            onAnonymousClick = {}
        )
    }
}

@Preview(name = "Registro AsistenTED", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewPantallaRegistro() {
    TemaAsistenTED(darkTheme = false) {
        PantallaAutenticacionContenido(
            modo = ModoAutenticacion.Registro,
            username = "Alex123",
            nombre = "Alexis",
            apellido = "Rodriguez",
            password = "rodriguez123",
            confirmation = "rodriguez12",
            usernameError = stringResource(R.string.auth_error_usuario_repetido),
            nombreError = null,
            apellidoError = null,
            passwordError = null,
            confirmationError = stringResource(R.string.auth_error_confirmacion_diferente),
            loginError = null,
            cargando = false,
            passwordVisible = false,
            confirmationVisible = false,
            onModoChange = {},
            onUsernameChange = {},
            onNombreChange = {},
            onApellidoChange = {},
            onPasswordChange = {},
            onConfirmationChange = {},
            onPasswordVisibilityChange = {},
            onConfirmationVisibilityChange = {},
            onPrimaryClick = {},
            onAnonymousClick = {}
        )
    }
}

@Preview(name = "Bienvenida AsistenTED", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewPantallaBienvenida() {
    TemaAsistenTED(darkTheme = false) {
        PantallaBienvenidaAsistenTED()
    }
}

@Composable
private fun BarraInferiorPrincipal(
    navController: NavHostController,
    avatarId: String
) {
    val entry by navController.currentBackStackEntryAsState()
    val current = entry?.destination?.route.orEmpty()
    val rutaSeleccionada = when {
        current.startsWith("procedure/") -> Rutas.HOME
        current == Rutas.HISTORY -> Rutas.PROFILE
        else -> current
    }
    val items = listOf(
        ElementoBarraInferior(Rutas.HOME, stringResource(R.string.nav_home), Icons.Default.Home),
        ElementoBarraInferior(Rutas.FAVORITES, stringResource(R.string.nav_favorites), Icons.Default.Bookmark),
        ElementoBarraInferior(Rutas.REMINDERS, stringResource(R.string.nav_notifications), Icons.Default.Notifications),
        ElementoBarraInferior(Rutas.PROFILE, stringResource(R.string.nav_profile), Icons.Default.Person)
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = rutaSeleccionada == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Rutas.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    if (item.route == Rutas.PROFILE) {
                        AvatarUsuario(
                            avatarId = avatarId,
                            modifier = Modifier
                                .size(28.dp)
                                .alpha(if (rutaSeleccionada == item.route) 1f else 0.72f)
                        )
                    } else {
                        Icon(
                            imageVector = item.icono,
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp),
                            tint = if (rutaSeleccionada == item.route) {
                                MaterialTheme.colorScheme.onSecondary
                            } else {
                                MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.72f)
                            }
                        )
                    }
                },
                label = { Text(item.label, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                    selectedTextColor = MaterialTheme.colorScheme.onSecondary,
                    indicatorColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.14f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.72f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.72f)
                )
            )
        }
    }
}

private data class ElementoBarraInferior(
    val route: String,
    val label: String,
    val icono: ImageVector
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







