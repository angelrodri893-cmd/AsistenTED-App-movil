package com.asistented.app.notificaciones

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.asistented.app.datos.modelos.Recordatorio

class ProgramadorRecordatorios(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun programar(reminder: Recordatorio) {
        val intent = Intent(context, ReceptorRecordatorio::class.java).apply {
            putExtra(ReceptorRecordatorio.EXTRA_TITLE, reminder.title)
            putExtra(ReceptorRecordatorio.EXTRA_NOTES, reminder.notes)
            putExtra(ReceptorRecordatorio.EXTRA_REMINDER_ID, reminder.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            reminder.programadoEnMillis,
            pendingIntent
        )
    }

    fun cancelar(reminder: Recordatorio) {
        val intent = Intent(context, ReceptorRecordatorio::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}


