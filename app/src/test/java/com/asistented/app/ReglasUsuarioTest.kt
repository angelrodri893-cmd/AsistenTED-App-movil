package com.asistented.app

import com.asistented.app.datos.modelos.ElementoHistorial
import com.asistented.app.datos.modelos.PerfilUsuario
import com.asistented.app.dominio.ReglasAutenticacion
import com.asistented.app.dominio.ReglasProgreso
import com.asistented.app.dominio.ReglasContenidoUsuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

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
}


