package com.asistented.app

import com.asistented.app.datos.modelos.ElementoHistorial
import com.asistented.app.datos.modelos.PerfilUsuario
import com.asistented.app.datos.CatalogoTramites
import com.asistented.app.dominio.ReglasAutenticacion
import com.asistented.app.dominio.ReglasProgreso
import com.asistented.app.dominio.ReglasContenidoUsuario
import com.asistented.app.interfaz.filtrarTramites
import com.asistented.app.interfaz.filtrarTramitesNotificaciones
import com.asistented.app.interfaz.combinarFechaHora
import com.asistented.app.interfaz.calcularProgresoDetalle
import com.asistented.app.interfaz.construirTextoGuia
import com.asistented.app.interfaz.seleccionarTramitesFavoritos
import com.asistented.app.interfaz.seleccionarTramitesHistorial
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class ReglasUsuarioTest {
    @Test
    fun usuarioAEmailInterno_normalizaSinExponerCorreo() {
        assertEquals("alexis_01@asistented.local", ReglasAutenticacion.usuarioAEmailInterno(" Alexis_01 "))
    }

    @Test
    fun validaciones_rechazanContrasenaCortaYAceptanUsuarioValido() {
        assertNull(ReglasAutenticacion.validarUsuario("liliana.vega"))
        assertTrue(ReglasAutenticacion.validarContrasena("123").orEmpty().contains("6"))
        assertFalse(ReglasAutenticacion.validarContrasena("123456", "123456").orEmpty().contains("no coinciden"))
    }

    @Test
    fun alternarPaso_marcaYDesmarcaPaso() {
        val marcado = ReglasProgreso.alternarPaso(emptySet(), "preparar")
        assertTrue(marcado.contains("preparar"))

        val desmarcado = ReglasProgreso.alternarPaso(marcado, "preparar")
        assertTrue(desmarcado.isEmpty())
    }

    @Test
    fun alternarFavorito_agregaYQuitaTramite() {
        val agregado = ReglasContenidoUsuario.alternarFavorito(emptyList(), "cedula")
        assertEquals(listOf("cedula"), agregado)

        val eliminado = ReglasContenidoUsuario.alternarFavorito(agregado, "cedula")
        assertTrue(eliminado.isEmpty())
    }

    @Test
    fun registrarHistorial_mueveTramiteRepetidoAlInicio() {
        val inicial = listOf(
            ElementoHistorial("ruc", 100),
            ElementoHistorial("cedula", 50)
        )
        val actualizado = ReglasContenidoUsuario.registrarHistorial(inicial, "cedula", 200)

        assertEquals("cedula", actualizado.first().tramiteId)
        assertEquals(2, actualizado.size)
        assertEquals(200, actualizado.first().consultadoEnMillis)
    }

    @Test
    fun perfilNuevo_usaAvatarLocalPorDefecto() {
        val perfil = PerfilUsuario(
            uid = "uid-1",
            username = "alexis",
            nombre = "Alexis",
            apellido = "Rodriguez"
        )

        assertEquals("azul", perfil.avatarId)
    }

    @Test
    fun filtrarTramites_buscaPorNombreEInstitucion() {
        val porNombre = filtrarTramites(CatalogoTramites.tramites, "LICENCIA", null)
        val porInstitucion = filtrarTramites(CatalogoTramites.tramites, "", "SRI")

        assertEquals(listOf("licencia"), porNombre.map { it.id })
        assertEquals(listOf("ruc", "impuestos"), porInstitucion.map { it.id })
    }

    @Test
    fun filtrarTramites_devuelveTodoOVacioSegunLaConsulta() {
        val todos = filtrarTramites(CatalogoTramites.tramites, "   ", null)
        val sinResultados = filtrarTramites(CatalogoTramites.tramites, "tramite inexistente", null)

        assertEquals(CatalogoTramites.tramites, todos)
        assertTrue(sinResultados.isEmpty())
    }

    @Test
    fun seleccionarTramitesFavoritos_conservaElOrdenDelCatalogo() {
        val seleccionados = seleccionarTramitesFavoritos(
            tramites = CatalogoTramites.tramites,
            favoritos = setOf("licencia", "cedula", "id-inexistente")
        )

        assertEquals(listOf("cedula", "licencia"), seleccionados.map { it.id })
    }

    @Test
    fun filtrarTramitesNotificaciones_buscaPorTituloOInstitucion() {
        val porTitulo = filtrarTramitesNotificaciones(CatalogoTramites.tramites, "pasaporte")
        val porInstitucion = filtrarTramitesNotificaciones(CatalogoTramites.tramites, "ANT")

        assertEquals(listOf("pasaporte"), porTitulo.map { it.id })
        assertEquals(listOf("licencia"), porInstitucion.map { it.id })
    }

    @Test
    fun combinarFechaHora_respetaLaZonaIndicada() {
        val resultado = combinarFechaHora(
            fecha = LocalDate.of(2026, 8, 5),
            hora = 20,
            minuto = 15,
            zona = ZoneOffset.UTC
        )

        assertEquals(Instant.parse("2026-08-05T20:15:00Z").toEpochMilli(), resultado)
    }

    @Test
    fun seleccionarTramitesHistorial_respetaOrdenYDescartaIdsInvalidos() {
        val seleccionados = seleccionarTramitesHistorial(
            tramites = CatalogoTramites.tramites,
            idsHistorial = listOf("licencia", "cedula", "licencia", "id-inexistente")
        )

        assertEquals(listOf("licencia", "cedula"), seleccionados.map { it.id })
    }

    @Test
    fun calcularProgresoDetalle_limitaElResultadoEntreCeroYUno() {
        assertEquals(0f, calcularProgresoDetalle(-1, 5))
        assertEquals(0.4f, calcularProgresoDetalle(2, 5))
        assertEquals(1f, calcularProgresoDetalle(8, 5))
        assertEquals(0f, calcularProgresoDetalle(2, 0))
    }

    @Test
    fun construirTextoGuia_incluyeResumenYPasos() {
        val tramite = CatalogoTramites.tramites.first()
        val texto = construirTextoGuia(tramite)

        assertTrue(texto.contains(tramite.summary))
        assertTrue(tramite.steps.all { texto.contains(it.title) && texto.contains(it.textoAyuda) })
    }
}


