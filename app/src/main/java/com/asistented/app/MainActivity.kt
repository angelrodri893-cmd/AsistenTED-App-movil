package com.asistented.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.asistented.app.ui.AsistenTedApp
import com.asistented.app.ui.AsistenTedController
import com.asistented.app.ui.theme.AsistenTEDv1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val controller = remember { AsistenTedController(applicationContext) }
            AsistenTEDv1Theme(accessibilitySettings = controller.accessibilitySettings) {
                AsistenTedApp(controller)
            }
        }
    }
}
