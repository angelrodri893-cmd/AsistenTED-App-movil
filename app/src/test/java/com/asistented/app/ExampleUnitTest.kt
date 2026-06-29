package com.asistented.app

import com.asistented.app.data.model.HistoryItem
import com.asistented.app.data.model.UserProfile
import com.asistented.app.domain.AuthRules
import com.asistented.app.domain.ProgressRules
import com.asistented.app.domain.UserContentRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun usernameToInternalEmail_normalizesWithoutExposingEmail() {
        assertEquals("alexis_01@asistented.local", AuthRules.usernameToInternalEmail(" Alexis_01 "))
    }

    @Test
    fun validation_rejectsShortPasswordAndAcceptsValidUser() {
        assertNull(AuthRules.validateUsername("liliana.vega"))
        assertTrue(AuthRules.validatePassword("123").orEmpty().contains("6"))
        assertFalse(AuthRules.validatePassword("123456", "123456").orEmpty().contains("no coinciden"))
    }

    @Test
    fun progressToggle_marksAndUnmarksStep() {
        val marked = ProgressRules.toggleStep(emptySet(), "preparar")
        assertTrue(marked.contains("preparar"))

        val unmarked = ProgressRules.toggleStep(marked, "preparar")
        assertTrue(unmarked.isEmpty())
    }

    @Test
    fun favoriteToggle_addsAndRemovesProcedure() {
        val added = UserContentRules.toggleFavorite(emptyList(), "cedula")
        assertEquals(listOf("cedula"), added)

        val removed = UserContentRules.toggleFavorite(added, "cedula")
        assertTrue(removed.isEmpty())
    }

    @Test
    fun historyRecord_movesRepeatedProcedureToTop() {
        val initial = listOf(
            HistoryItem("ruc", 100),
            HistoryItem("cedula", 50)
        )
        val updated = UserContentRules.recordHistory(initial, "cedula", 200)

        assertEquals("cedula", updated.first().procedureId)
        assertEquals(2, updated.size)
        assertEquals(200, updated.first().consultedAtMillis)
    }

    @Test
    fun newProfile_usesLocalAvatarByDefault() {
        val profile = UserProfile(
            uid = "uid-1",
            username = "alexis",
            firstName = "Alexis",
            lastName = "Rodriguez"
        )

        assertEquals("azul", profile.avatarId)
    }
}
