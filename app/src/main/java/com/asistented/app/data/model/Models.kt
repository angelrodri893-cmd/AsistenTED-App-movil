package com.asistented.app.data.model

data class AccessibilitySettings(
    val largeText: Boolean = false,
    val highContrast: Boolean = false
)

data class UserProfile(
    val uid: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val avatarId: String = "azul",
    val isGuest: Boolean = false,
    val accessibility: AccessibilitySettings = AccessibilitySettings()
) {
    val displayName: String
        get() = listOf(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { if (isGuest) "Anónimo" else username }
}

data class GuideStep(
    val id: String,
    val title: String,
    val description: String,
    val helpText: String,
    val checkItems: List<String>,
    val imagePlaceholder: String
)

data class Procedure(
    val id: String,
    val title: String,
    val institution: String,
    val summary: String,
    val category: String,
    val officialUrl: String,
    val steps: List<GuideStep>
)

data class HistoryItem(
    val procedureId: String,
    val consultedAtMillis: Long
)

data class Reminder(
    val id: String,
    val procedureId: String,
    val title: String,
    val notes: String,
    val scheduledAtMillis: Long
)

data class ForumComment(
    val id: String,
    val procedureId: String,
    val userId: String,
    val username: String,
    val text: String,
    val createdAtMillis: Long
)
