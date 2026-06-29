package com.asistented.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.asistented.app.interfaz.AplicacionAsistenTed
import com.asistented.app.interfaz.ControladorAsistenTed
import com.asistented.app.interfaz.tema.TemaAsistenTED

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val controlador = remember { ControladorAsistenTed(applicationContext) }
            TemaAsistenTED(configuracionAccesibilidad = controlador.configuracionAccesibilidad) {
                AplicacionAsistenTed(controlador)
            }
        }
    }
}


