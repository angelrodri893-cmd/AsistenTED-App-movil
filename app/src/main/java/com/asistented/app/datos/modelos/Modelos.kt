package com.asistented.app.datos.modelos

data class ConfiguracionAccesibilidad(
    val textoGrande: Boolean = false,
    val altoContraste: Boolean = false
)

data class PerfilUsuario(
    val uid: String,
    val username: String,
    val nombre: String,
    val apellido: String,
    val avatarId: String = "azul",
    val esInvitado: Boolean = false,
    val accessibility: ConfiguracionAccesibilidad = ConfiguracionAccesibilidad()
) {
    val nombreVisible: String
        get() = listOf(nombre, apellido)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { if (esInvitado) "Anónimo" else username }
}

data class PasoGuia(
    val id: String,
    val title: String,
    val description: String,
    val textoAyuda: String,
    val elementosRevision: List<String>,
    val espacioImagen: String
)

data class Tramite(
    val id: String,
    val title: String,
    val institution: String,
    val summary: String,
    val category: String,
    val urlOficial: String,
    val steps: List<PasoGuia>
)

data class ElementoHistorial(
    val tramiteId: String,
    val consultadoEnMillis: Long
)

data class Recordatorio(
    val id: String,
    val tramiteId: String,
    val title: String,
    val notes: String,
    val programadoEnMillis: Long
)

data class ComentarioForo(
    val id: String,
    val tramiteId: String,
    val userId: String,
    val username: String,
    val text: String,
    val createdAtMillis: Long
)


