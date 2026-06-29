package com.asistented.app

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

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
}


