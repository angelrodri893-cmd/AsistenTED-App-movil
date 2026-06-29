package com.asistented.app.dominio

import com.asistented.app.datos.modelos.ElementoHistorial

object ReglasProgreso {
    fun alternarPaso(pasosCompletadosIds: Set<String>, stepId: String): Set<String> =
        if (pasosCompletadosIds.contains(stepId)) {
            pasosCompletadosIds - stepId
        } else {
            pasosCompletadosIds + stepId
        }
}

object ReglasContenidoUsuario {
    fun alternarFavorito(favoritos: List<String>, tramiteId: String): List<String> =
        if (favoritos.contains(tramiteId)) {
            favoritos - tramiteId
        } else {
            favoritos + tramiteId
        }

    fun registrarHistorial(
        historial: List<ElementoHistorial>,
        tramiteId: String,
        consultadoEnMillis: Long
    ): List<ElementoHistorial> =
        listOf(ElementoHistorial(tramiteId, consultadoEnMillis)) + historial.filterNot { it.tramiteId == tramiteId }
}


