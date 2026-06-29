package com.asistented.app.domain

import com.asistented.app.data.model.HistoryItem

object ProgressRules {
    fun toggleStep(completedStepIds: Set<String>, stepId: String): Set<String> =
        if (completedStepIds.contains(stepId)) {
            completedStepIds - stepId
        } else {
            completedStepIds + stepId
        }
}

object UserContentRules {
    fun toggleFavorite(favorites: List<String>, procedureId: String): List<String> =
        if (favorites.contains(procedureId)) {
            favorites - procedureId
        } else {
            favorites + procedureId
        }

    fun recordHistory(
        history: List<HistoryItem>,
        procedureId: String,
        consultedAtMillis: Long
    ): List<HistoryItem> =
        listOf(HistoryItem(procedureId, consultedAtMillis)) + history.filterNot { it.procedureId == procedureId }
}
