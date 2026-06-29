package com.asistented.app.datos

import com.asistented.app.datos.modelos.ConfiguracionAccesibilidad
import com.asistented.app.datos.modelos.ComentarioForo
import com.asistented.app.datos.modelos.ElementoHistorial
import com.asistented.app.datos.modelos.Recordatorio
import com.asistented.app.datos.modelos.PerfilUsuario
import com.asistented.app.dominio.ReglasAutenticacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

class RepositorioAutenticacion(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val repositorioUsuario: RepositorioUsuario = RepositorioUsuario()
) {
    fun registrar(
        username: String,
        nombre: String,
        apellido: String,
        password: String,
        onResult: (Result<PerfilUsuario>) -> Unit
    ) {
        val normalized = ReglasAutenticacion.normalizarUsuario(username)
        auth.createUserWithEmailAndPassword(ReglasAutenticacion.usuarioAEmailInterno(normalized), password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid.orEmpty()
                val perfil = PerfilUsuario(
                    uid = uid,
                    username = normalized,
                    nombre = nombre.trim(),
                    apellido = apellido.trim()
                )
                repositorioUsuario.guardarPerfil(perfil) { saveResult ->
                    onResult(saveResult.map { perfil })
                }
            }
            .addOnFailureListener { error ->
                onResult(Result.failure(error))
            }
    }

    fun iniciarSesion(username: String, password: String, onResult: (Result<PerfilUsuario>) -> Unit) {
        val normalized = ReglasAutenticacion.normalizarUsuario(username)
        auth.signInWithEmailAndPassword(ReglasAutenticacion.usuarioAEmailInterno(normalized), password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid.orEmpty()
                repositorioUsuario.cargarPerfil(uid) { perfilResult ->
                    perfilResult
                        .onSuccess { perfil ->
                            onResult(Result.success(perfil.copy(username = perfil.username.ifBlank { normalized })))
                        }
                        .onFailure {
                            val perfilBasico = PerfilUsuario(
                                uid = uid,
                                username = normalized,
                                nombre = normalized,
                                apellido = ""
                            )
                            repositorioUsuario.guardarPerfil(perfilBasico) {
                                onResult(Result.success(perfilBasico))
                            }
                        }
                }
            }
            .addOnFailureListener { error ->
                onResult(Result.failure(error))
            }
    }

    fun usuarioActual(onResult: (PerfilUsuario?) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onResult(null)
            return
        }
        repositorioUsuario.cargarPerfil(uid) { result ->
            val usuarioDesdeAuth = auth.currentUser?.email
                ?.substringBefore("@")
                ?.ifBlank { "usuario" }
                ?: "usuario"
            onResult(
                result.getOrElse {
                    PerfilUsuario(
                        uid = uid,
                        username = usuarioDesdeAuth,
                        nombre = usuarioDesdeAuth,
                        apellido = ""
                    )
                }
            )
        }
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    fun perfilInvitado(): PerfilUsuario = PerfilUsuario(
        uid = "guest",
        username = "anonimo",
        nombre = "Anónimo",
        apellido = "",
        esInvitado = true
    )
}

class RepositorioUsuario(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun guardarPerfil(perfil: PerfilUsuario, onResult: (Result<Unit>) -> Unit = {}) {
        val data = mapOf(
            "uid" to perfil.uid,
            "username" to perfil.username,
            "nombre" to perfil.nombre,
            "apellido" to perfil.apellido,
            "avatarId" to perfil.avatarId,
            "esInvitado" to perfil.esInvitado,
            "textoGrande" to perfil.accessibility.textoGrande,
            "altoContraste" to perfil.accessibility.altoContraste
        )
        db.collection("users").document(perfil.uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun cargarPerfil(uid: String, onResult: (Result<PerfilUsuario>) -> Unit) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onResult(Result.failure(IllegalStateException("No se encontró el perfil.")))
                    return@addOnSuccessListener
                }
                onResult(
                    Result.success(
                        PerfilUsuario(
                            uid = uid,
                            username = document.getString("username").orEmpty(),
                            nombre = document.getString("nombre").orEmpty(),
                            apellido = document.getString("apellido").orEmpty(),
                            avatarId = document.getString("avatarId").orEmpty().ifBlank { "azul" },
                            esInvitado = document.getBoolean("esInvitado") ?: false,
                            accessibility = ConfiguracionAccesibilidad(
                                textoGrande = document.getBoolean("textoGrande") ?: false,
                                altoContraste = document.getBoolean("altoContraste") ?: false
                            )
                        )
                    )
                )
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun guardarFavorito(uid: String, tramiteId: String, enabled: Boolean) {
        if (uid == "guest") return
        val operation = if (enabled) {
            FieldValue.arrayUnion(tramiteId)
        } else {
            FieldValue.arrayRemove(tramiteId)
        }
        db.collection("users").document(uid).set(mapOf("favoritos" to operation), SetOptions.merge())
    }

    fun cargarFavoritos(uid: String, onResult: (List<String>) -> Unit) {
        if (uid == "guest") {
            onResult(emptyList())
            return
        }
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                val favoritos = document.get("favoritos") as? List<*>
                onResult(favoritos.orEmpty().filterIsInstance<String>())
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun guardarProgreso(uid: String, tramiteId: String, pasosCompletadosIds: Set<String>) {
        if (uid == "guest") return
        db.collection("users").document(uid)
            .collection("progress")
            .document(tramiteId)
            .set(
                mapOf(
                    "pasosCompletados" to pasosCompletadosIds.toList(),
                    "updatedAtMillis" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
    }

    fun cargarProgreso(uid: String, onResult: (Map<String, Set<String>>) -> Unit) {
        if (uid == "guest") {
            onResult(emptyMap())
            return
        }
        db.collection("users").document(uid)
            .collection("progress")
            .get()
            .addOnSuccessListener { snapshot ->
                val progress = snapshot.documents.associate { document ->
                    val steps = document.get("pasosCompletados") as? List<*>
                    document.id to steps.orEmpty().filterIsInstance<String>().toSet()
                }
                onResult(progress)
            }
            .addOnFailureListener { onResult(emptyMap()) }
    }

    fun agregarHistorial(uid: String, item: ElementoHistorial) {
        if (uid == "guest") return
        db.collection("users").document(uid)
            .collection("historial")
            .document("${item.tramiteId}_${item.consultadoEnMillis}")
            .set(item)
    }

    fun cargarHistorial(uid: String, onResult: (List<ElementoHistorial>) -> Unit) {
        if (uid == "guest") {
            onResult(emptyList())
            return
        }
        db.collection("users").document(uid)
            .collection("historial")
            .orderBy("consultadoEnMillis", Query.Direction.DESCENDING)
            .limit(30)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(
                    snapshot.documents.mapNotNull { document ->
                        val tramiteId = document.getString("tramiteId") ?: return@mapNotNull null
                        val consultedAt = document.getLong("consultadoEnMillis") ?: return@mapNotNull null
                        ElementoHistorial(tramiteId, consultedAt)
                    }
                )
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun guardarRecordatorio(uid: String, reminder: Recordatorio) {
        if (uid == "guest") return
        db.collection("users").document(uid)
            .collection("recordatorios")
            .document(reminder.id)
            .set(reminder, SetOptions.merge())
    }

    fun cargarRecordatorios(uid: String, onResult: (List<Recordatorio>) -> Unit) {
        if (uid == "guest") {
            onResult(emptyList())
            return
        }
        db.collection("users").document(uid)
            .collection("recordatorios")
            .orderBy("programadoEnMillis", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(
                    snapshot.documents.mapNotNull { document ->
                        val id = document.getString("id") ?: document.id
                        val tramiteId = document.getString("tramiteId") ?: return@mapNotNull null
                        val title = document.getString("title") ?: return@mapNotNull null
                        val notes = document.getString("notes").orEmpty()
                        val scheduledAt = document.getLong("programadoEnMillis") ?: return@mapNotNull null
                        Recordatorio(id, tramiteId, title, notes, scheduledAt)
                    }
                )
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun borrarRecordatorio(uid: String, reminderId: String) {
        if (uid == "guest") return
        db.collection("users").document(uid)
            .collection("recordatorios")
            .document(reminderId)
            .delete()
    }
}

class RepositorioForo(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun publicar(comment: ComentarioForo, onResult: (Result<Unit>) -> Unit = {}) {
        db.collection("tramites")
            .document(comment.tramiteId)
            .collection("comentarios")
            .document(comment.id)
            .set(comment)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun cargar(tramiteId: String, onResult: (List<ComentarioForo>) -> Unit) {
        db.collection("tramites")
            .document(tramiteId)
            .collection("comentarios")
            .orderBy("createdAtMillis", Query.Direction.ASCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(
                    snapshot.documents.mapNotNull { document ->
                        val id = document.getString("id") ?: document.id
                        val userId = document.getString("userId") ?: return@mapNotNull null
                        val username = document.getString("username") ?: "Usuario"
                        val text = document.getString("text") ?: return@mapNotNull null
                        val createdAt = document.getLong("createdAtMillis") ?: return@mapNotNull null
                        ComentarioForo(id, tramiteId, userId, username, text, createdAt)
                    }
                )
            }
            .addOnFailureListener { onResult(emptyList()) }
    }
}


