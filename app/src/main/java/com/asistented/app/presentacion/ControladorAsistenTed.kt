package com.asistented.app.presentacion

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.asistented.app.datos.PreferenciasLocales
import com.asistented.app.datos.RepositorioAutenticacion
import com.asistented.app.datos.RepositorioForo
import com.asistented.app.datos.RepositorioUsuario
import com.asistented.app.datos.gobec.RepositorioCatalogoTramites
import com.asistented.app.datos.modelos.ComentarioForo
import com.asistented.app.datos.modelos.ConfiguracionAccesibilidad
import com.asistented.app.datos.modelos.ElementoHistorial
import com.asistented.app.datos.modelos.PerfilUsuario
import com.asistented.app.datos.modelos.Recordatorio
import com.asistented.app.datos.modelos.Tramite
import com.asistented.app.dominio.ReglasAutenticacion
import com.asistented.app.dominio.ReglasContenidoUsuario
import com.asistented.app.dominio.ReglasProgreso
import com.asistented.app.notificaciones.ProgramadorRecordatorios
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

enum class EstadoCatalogo {
    CARGANDO,
    DISPONIBLE,
    ERROR_CONEXION
}

class ControladorAsistenTed(context: Context) {
    private val appContext = context.applicationContext
    private val preferenciasLocales = PreferenciasLocales(appContext)
    private val repositorioAutenticacion = RepositorioAutenticacion()
    private val repositorioUsuario = RepositorioUsuario()
    private val repositorioForo = RepositorioForo()
    private val repositorioCatalogo = RepositorioCatalogoTramites()
    private val programadorRecordatorios = ProgramadorRecordatorios(appContext)
    private val alcanceTrabajo = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val tramites = mutableStateListOf<Tramite>()
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
    var estadoCatalogo by mutableStateOf(EstadoCatalogo.CARGANDO)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set
    var mostrarAyudaPrincipal by mutableStateOf(false)
        private set
    var mostrarAyudaFavoritos by mutableStateOf(false)
        private set
    var mostrarAyudaNotificaciones by mutableStateOf(false)
        private set
    var mostrarAyudaPerfil by mutableStateOf(false)
        private set
    var mostrarAyudaDetalle by mutableStateOf(false)
        private set

    init {
        actualizarCatalogoGobEc()
        repositorioAutenticacion.usuarioActual { perfil ->
            perfil?.let { establecerUsuarioRegistrado(it) }
        }
    }

    fun limpiarMensaje() { mensaje = null }

    fun mostrarMensaje(text: String) { mensaje = text }

    fun buscarTramite(id: String): Tramite? = tramites.firstOrNull { it.id == id }

    fun actualizarCatalogoGobEc() {
        if (estadoCatalogo == EstadoCatalogo.CARGANDO && tramites.isNotEmpty()) return
        estadoCatalogo = EstadoCatalogo.CARGANDO
        tramites.clear()
        alcanceTrabajo.launch {
            runCatching { repositorioCatalogo.cargarCatalogo() }
                .onSuccess { catalogoActualizado ->
                    tramites.addAll(catalogoActualizado)
                    estadoCatalogo = EstadoCatalogo.DISPONIBLE
                    mensaje = null
                }
                .onFailure {
                    tramites.clear()
                    estadoCatalogo = EstadoCatalogo.ERROR_CONEXION
                }
        }
    }

    fun entrarComoInvitado() {
        usuarioActual = repositorioAutenticacion.perfilInvitado().copy(accessibility = configuracionAccesibilidad)
        limpiarAyudas()
        limpiarDatosDeUsuario()
        mensaje = "Entraste como anónimo. Puedes ver guías, pero algunas funciones se guardan solo con cuenta."
    }

    fun registrar(
        username: String,
        nombre: String,
        apellido: String,
        password: String,
        confirmation: String,
        onError: (Throwable) -> Unit = {}
    ) {
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
                    preferenciasLocales.marcarAyudaPrincipalPendiente(it.uid)
                    preferenciasLocales.marcarAyudaFavoritosPendiente(it.uid)
                    preferenciasLocales.marcarAyudaNotificacionesPendiente(it.uid)
                    preferenciasLocales.marcarAyudaPerfilPendiente(it.uid)
                    preferenciasLocales.marcarAyudaDetallePendiente(it.uid)
                    establecerUsuarioRegistrado(it)
                    mensaje = "Cuenta creada correctamente."
                }
                .onFailure {
                    mensaje = it.comoMensajeUsuario("No se pudo crear la cuenta.")
                    onError(it)
                }
        }
    }

    fun iniciarSesion(username: String, password: String, onError: (Throwable) -> Unit = {}) {
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
                .onFailure {
                    mensaje = it.comoMensajeUsuario("Usuario o contraseña incorrectos.")
                    onError(it)
                }
        }
    }

    fun cerrarSesion() {
        repositorioAutenticacion.cerrarSesion()
        usuarioActual = null
        limpiarAyudas()
        limpiarDatosDeUsuario()
    }

    fun descartarAyudaPrincipal() {
        usuarioActual?.takeIf { !it.esInvitado }?.let {
            preferenciasLocales.completarAyudaPrincipal(it.uid)
            mostrarAyudaPrincipal = false
        }
    }

    fun descartarAyudaFavoritos() {
        usuarioActual?.takeIf { !it.esInvitado }?.let {
            preferenciasLocales.completarAyudaFavoritos(it.uid)
            mostrarAyudaFavoritos = false
        }
    }

    fun descartarAyudaNotificaciones() {
        usuarioActual?.takeIf { !it.esInvitado }?.let {
            preferenciasLocales.completarAyudaNotificaciones(it.uid)
            mostrarAyudaNotificaciones = false
        }
    }

    fun descartarAyudaPerfil() {
        usuarioActual?.takeIf { !it.esInvitado }?.let {
            preferenciasLocales.completarAyudaPerfil(it.uid)
            mostrarAyudaPerfil = false
        }
    }

    fun descartarAyudaDetalle() {
        usuarioActual?.takeIf { !it.esInvitado }?.let {
            preferenciasLocales.completarAyudaDetalle(it.uid)
            mostrarAyudaDetalle = false
        }
    }

    fun actualizarAccesibilidad(configuracion: ConfiguracionAccesibilidad) {
        configuracionAccesibilidad = configuracion
        preferenciasLocales.guardarAccesibilidad(configuracion)
        usuarioActual = usuarioActual?.copy(accessibility = configuracion)
        usuarioActual?.takeIf { !it.esInvitado }?.let { repositorioUsuario.guardarPerfil(it) }
    }

    fun actualizarPerfil(nombre: String, apellido: String, avatarId: String, onResultado: (Boolean) -> Unit = {}) {
        val usuario = usuarioActual ?: return
        if (usuario.esInvitado) {
            mensaje = "Crea una cuenta para guardar tu perfil."
            onResultado(false)
            return
        }
        val perfilActualizado = usuario.copy(nombre = nombre.trim(), apellido = apellido.trim(), avatarId = avatarId)
        repositorioUsuario.guardarPerfil(perfilActualizado) { resultado ->
            if (resultado.isSuccess) {
                usuarioActual = perfilActualizado
                mensaje = "Perfil guardado."
            } else {
                mensaje = "No se pudo guardar el perfil."
            }
            onResultado(resultado.isSuccess)
        }
    }

    fun cargarComentarios(tramiteId: String) {
        repositorioForo.cargar(tramiteId) { loaded -> comentarios[tramiteId] = loaded }
    }

    fun marcarConsultado(tramiteId: String) {
        val updated = ReglasContenidoUsuario.registrarHistorial(historial, tramiteId, System.currentTimeMillis())
        historial.clear()
        historial.addAll(updated)
        usuarioActual?.let { repositorioUsuario.agregarHistorial(it.uid, updated.first()) }
    }

    fun alternarFavorito(tramiteId: String) {
        val user = usuarioActual ?: return
        if (user.esInvitado) {
            mensaje = "Inicia sesión para guardar favoritos."
            return
        }
        val updated = ReglasContenidoUsuario.alternarFavorito(favoritos, tramiteId)
        favoritos.clear()
        favoritos.addAll(updated)
        repositorioUsuario.guardarFavorito(user.uid, tramiteId, tramiteId in updated) { result ->
            if (result.isFailure) mensaje = "No se pudo actualizar favoritos."
        }
    }

    fun alternarPaso(tramiteId: String, stepId: String) {
        val updated = ReglasProgreso.alternarPaso(pasosCompletados[tramiteId].orEmpty(), stepId)
        pasosCompletados[tramiteId] = updated
        usuarioActual?.let {
            repositorioUsuario.guardarProgreso(it.uid, tramiteId, updated) { result ->
                if (result.isFailure) mensaje = "No se pudo guardar el progreso."
            }
        }
    }

    fun agregarComentario(tramiteId: String, text: String) {
        val user = usuarioActual ?: return
        if (user.esInvitado) {
            mensaje = "Inicia sesión para comentar en el foro."
            return
        }
        val normalizedText = text.trim()
        if (normalizedText.isBlank() || normalizedText.length > MAX_COMMENT_LENGTH) {
            mensaje = "El comentario debe tener entre 1 y $MAX_COMMENT_LENGTH caracteres."
            return
        }
        val comment = ComentarioForo(UUID.randomUUID().toString(), tramiteId, user.uid, user.nombreVisible, normalizedText, System.currentTimeMillis())
        repositorioForo.publicar(comment) { result ->
            if (result.isSuccess) {
                comentarios[tramiteId] = comentarios[tramiteId].orEmpty() + comment
                mensaje = "Comentario publicado."
            } else {
                mensaje = "No se pudo publicar el comentario."
            }
        }
    }

    fun responderComentario(comentarioPadre: ComentarioForo, text: String) {
        val user = usuarioActual ?: return
        if (user.esInvitado) {
            mensaje = "Inicia sesión para responder en el foro."
            return
        }
        val normalizedText = text.trim()
        if (normalizedText.isBlank() || normalizedText.length > MAX_COMMENT_LENGTH) {
            mensaje = "La respuesta debe tener entre 1 y $MAX_COMMENT_LENGTH caracteres."
            return
        }
        val respuesta = ComentarioForo(UUID.randomUUID().toString(), comentarioPadre.tramiteId, user.uid, user.nombreVisible, normalizedText, System.currentTimeMillis(), comentarioPadre.id)
        repositorioForo.publicar(respuesta) { result ->
            if (result.isSuccess) {
                comentarios[comentarioPadre.tramiteId] = comentarios[comentarioPadre.tramiteId].orEmpty() + respuesta
                mensaje = "Respuesta publicada."
            } else {
                mensaje = "No se pudo publicar la respuesta."
            }
        }
    }

    fun editarComentario(comment: ComentarioForo, nuevoTexto: String) {
        val user = usuarioActual ?: return
        if (comment.userId != user.uid) {
            mensaje = "Solo puedes editar tus propios comentarios."
            return
        }
        val normalizedText = nuevoTexto.trim()
        if (normalizedText.isBlank() || normalizedText.length > MAX_COMMENT_LENGTH) {
            mensaje = "El comentario debe tener entre 1 y $MAX_COMMENT_LENGTH caracteres."
            return
        }
        repositorioForo.editar(comment, normalizedText) { result ->
            if (result.isSuccess) {
                val actualizado = comment.copy(text = normalizedText, editadoEnMillis = System.currentTimeMillis())
                comentarios[comment.tramiteId] = comentarios[comment.tramiteId].orEmpty().map { if (it.id == comment.id) actualizado else it }
                mensaje = "Comentario editado."
            } else {
                mensaje = "No se pudo editar el comentario."
            }
        }
    }

    fun eliminarComentario(comment: ComentarioForo) {
        val user = usuarioActual ?: return
        if (comment.userId != user.uid) {
            mensaje = "Solo puedes eliminar tus propios comentarios."
            return
        }
        repositorioForo.eliminar(comment) { result ->
            if (result.isSuccess) {
                comentarios[comment.tramiteId] = comentarios[comment.tramiteId].orEmpty().filterNot { it.id == comment.id || it.respuestaAId == comment.id }
                mensaje = "Comentario eliminado."
            } else {
                mensaje = "No se pudo eliminar el comentario."
            }
        }
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
        val reminder = Recordatorio(UUID.randomUUID().toString(), tramiteId, title.trim().ifBlank { "Recordatorio de trámite" }, notes.trim(), programadoEnMillis)
        repositorioUsuario.guardarRecordatorio(user.uid, reminder) { result ->
            if (result.isSuccess) {
                recordatorios.add(0, reminder)
                programadorRecordatorios.programar(reminder)
                mensaje = "Recordatorio guardado."
            } else {
                mensaje = "No se pudo guardar el recordatorio."
            }
        }
    }

    fun borrarRecordatorio(reminder: Recordatorio) {
        val user = usuarioActual ?: return
        repositorioUsuario.borrarRecordatorio(user.uid, reminder.id) { result ->
            if (result.isSuccess) {
                recordatorios.remove(reminder)
                programadorRecordatorios.cancelar(reminder)
                mensaje = "Recordatorio eliminado."
            } else {
                mensaje = "No se pudo eliminar el recordatorio."
            }
        }
    }

    private fun establecerUsuarioRegistrado(perfil: PerfilUsuario) {
        val usuarioRegistrado = perfil.copy(accessibility = configuracionAccesibilidad)
        usuarioActual = usuarioRegistrado
        mostrarAyudaPrincipal = preferenciasLocales.debeMostrarAyudaPrincipal(usuarioRegistrado.uid)
        mostrarAyudaFavoritos = preferenciasLocales.debeMostrarAyudaFavoritos(usuarioRegistrado.uid)
        mostrarAyudaNotificaciones = preferenciasLocales.debeMostrarAyudaNotificaciones(usuarioRegistrado.uid)
        mostrarAyudaPerfil = preferenciasLocales.debeMostrarAyudaPerfil(usuarioRegistrado.uid)
        mostrarAyudaDetalle = preferenciasLocales.debeMostrarAyudaDetalle(usuarioRegistrado.uid)
        cargarDatosPersistidos(usuarioRegistrado.uid)
    }

    private fun cargarDatosPersistidos(uid: String) {
        repositorioUsuario.cargarFavoritos(uid) { loaded -> favoritos.clear(); favoritos.addAll(loaded) }
        repositorioUsuario.cargarHistorial(uid) { loaded -> historial.clear(); historial.addAll(loaded.distinctBy { it.tramiteId }) }
        repositorioUsuario.cargarProgreso(uid) { loaded -> pasosCompletados.apply { clear(); putAll(loaded) } }
        repositorioUsuario.cargarRecordatorios(uid) { loaded -> recordatorios.clear(); recordatorios.addAll(loaded) }
    }

    private fun limpiarAyudas() {
        mostrarAyudaPrincipal = false
        mostrarAyudaFavoritos = false
        mostrarAyudaNotificaciones = false
        mostrarAyudaPerfil = false
        mostrarAyudaDetalle = false
    }

    private fun limpiarDatosDeUsuario() {
        favoritos.clear()
        historial.clear()
        recordatorios.clear()
        pasosCompletados.clear()
        comentarios.clear()
    }

    private companion object {
        const val MAX_COMMENT_LENGTH = 1000
    }
}

internal fun Throwable.comoMensajeUsuario(defaultMessage: String): String {
    val raw = (localizedMessage ?: message).orEmpty()
    return when {
        raw.contains("PERMISSION_DENIED", ignoreCase = true) ||
            raw.contains("permission_denied", ignoreCase = true) ||
            raw.contains("Missing or insufficient permissions", ignoreCase = true) ->
            "Faltan permisos en Firestore. Publica las reglas de seguridad del proyecto y vuelve a intentar."
        else -> raw.ifBlank { defaultMessage }
    }
}

internal fun Throwable.esUsuarioDuplicadoEnAuth(): Boolean {
    val raw = (localizedMessage ?: message).orEmpty()
    return this is FirebaseAuthUserCollisionException ||
        (this as? FirebaseAuthException)?.errorCode == "ERROR_EMAIL_ALREADY_IN_USE" ||
        raw.contains("email address is already in use", ignoreCase = true) ||
        raw.contains("already in use", ignoreCase = true)
}
