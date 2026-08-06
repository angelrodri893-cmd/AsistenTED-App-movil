package com.asistented.app

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asistented.app.datos.PreferenciasLocales

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Prueba instrumentada que se ejecuta en un dispositivo Android.
 *
 * Consulta la documentacion de pruebas en http://d.android.com/tools/testing.
 */
@RunWith(AndroidJUnit4::class)
class ContextoAplicacionTest {
    @Test
    fun usaContextoDeLaAplicacion() {
        val contextoAplicacion = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.asistented.app", contextoAplicacion.packageName)
    }

    @Test
    fun ayudaPrincipal_seMantieneHastaQueElUsuarioLaComplete() {
        val contextoAplicacion = InstrumentationRegistry.getInstrumentation().targetContext
        val preferencias = PreferenciasLocales(contextoAplicacion)
        val uidPrueba = "usuario_prueba_ayuda_principal"

        preferencias.completarAyudaPrincipal(uidPrueba)
        assertFalse(preferencias.debeMostrarAyudaPrincipal(uidPrueba))

        preferencias.marcarAyudaPrincipalPendiente(uidPrueba)
        assertTrue(PreferenciasLocales(contextoAplicacion).debeMostrarAyudaPrincipal(uidPrueba))

        preferencias.completarAyudaPrincipal(uidPrueba)
        assertFalse(preferencias.debeMostrarAyudaPrincipal(uidPrueba))
        assertFalse(preferencias.debeMostrarAyudaPrincipal(""))
    }

    @Test
    fun ayudaFavoritos_seMantieneHastaQueElUsuarioLaComplete() {
        val contextoAplicacion = InstrumentationRegistry.getInstrumentation().targetContext
        val preferencias = PreferenciasLocales(contextoAplicacion)
        val uidPrueba = "usuario_prueba_ayuda_favoritos"

        preferencias.completarAyudaFavoritos(uidPrueba)
        assertFalse(preferencias.debeMostrarAyudaFavoritos(uidPrueba))

        preferencias.marcarAyudaFavoritosPendiente(uidPrueba)
        assertTrue(PreferenciasLocales(contextoAplicacion).debeMostrarAyudaFavoritos(uidPrueba))

        preferencias.completarAyudaFavoritos(uidPrueba)
        assertFalse(preferencias.debeMostrarAyudaFavoritos(uidPrueba))
        assertFalse(preferencias.debeMostrarAyudaFavoritos(""))
    }

    @Test
    fun ayudaNotificaciones_seMantieneHastaQueElUsuarioLaComplete() {
        val contextoAplicacion = InstrumentationRegistry.getInstrumentation().targetContext
        val preferencias = PreferenciasLocales(contextoAplicacion)
        val uidPrueba = "usuario_prueba_ayuda_notificaciones"

        preferencias.completarAyudaNotificaciones(uidPrueba)
        assertFalse(preferencias.debeMostrarAyudaNotificaciones(uidPrueba))

        preferencias.marcarAyudaNotificacionesPendiente(uidPrueba)
        assertTrue(PreferenciasLocales(contextoAplicacion).debeMostrarAyudaNotificaciones(uidPrueba))

        preferencias.completarAyudaNotificaciones(uidPrueba)
        assertFalse(preferencias.debeMostrarAyudaNotificaciones(uidPrueba))
        assertFalse(preferencias.debeMostrarAyudaNotificaciones(""))
    }

    @Test
    fun ayudaPerfil_seMantieneHastaQueElUsuarioLaComplete() {
        val contextoAplicacion = InstrumentationRegistry.getInstrumentation().targetContext
        val preferencias = PreferenciasLocales(contextoAplicacion)
        val uidPrueba = "usuario_prueba_ayuda_perfil"

        preferencias.completarAyudaPerfil(uidPrueba)
        assertFalse(preferencias.debeMostrarAyudaPerfil(uidPrueba))

        preferencias.marcarAyudaPerfilPendiente(uidPrueba)
        assertTrue(PreferenciasLocales(contextoAplicacion).debeMostrarAyudaPerfil(uidPrueba))

        preferencias.completarAyudaPerfil(uidPrueba)
        assertFalse(preferencias.debeMostrarAyudaPerfil(uidPrueba))
        assertFalse(preferencias.debeMostrarAyudaPerfil(""))
    }
}


