package com.asistented.app.notificaciones

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.asistented.app.datos.RepositorioUsuario
import com.google.firebase.auth.FirebaseAuth

class ReceptorInicioDispositivo : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val resultadoAsincrono = goAsync()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            resultadoAsincrono.finish()
            return
        }

        RepositorioUsuario().cargarRecordatorios(uid) { recordatorios ->
            val programador = ProgramadorRecordatorios(context.applicationContext)
            recordatorios
                .filter { it.programadoEnMillis > System.currentTimeMillis() }
                .forEach(programador::programar)
            resultadoAsincrono.finish()
        }
    }
}
