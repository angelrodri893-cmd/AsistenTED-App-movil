package com.asistented.app.domain

object AuthRules {
    private val usernameRegex = Regex("^[a-z0-9._-]{3,24}$")

    fun normalizeUsername(username: String): String =
        username.trim().lowercase().replace("\\s+".toRegex(), "")

    fun usernameToInternalEmail(username: String): String =
        "${normalizeUsername(username)}@asistented.local"

    fun validateUsername(username: String): String? {
        val normalized = normalizeUsername(username)
        return when {
            normalized.isBlank() -> "Ingresa un nombre de usuario."
            !usernameRegex.matches(normalized) -> "Usa 3 a 24 caracteres: letras, números, punto, guion o guion bajo."
            else -> null
        }
    }

    fun validatePassword(password: String, confirmation: String? = null): String? {
        return when {
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
            confirmation != null && password != confirmation -> "Las contraseñas no coinciden."
            else -> null
        }
    }
}
