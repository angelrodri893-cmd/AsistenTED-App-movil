package com.asistented.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.asistented.app.MainActivity
import com.asistented.app.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ensureChannel(context)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Recordatorio de trámite"
        val notes = intent.getStringExtra(EXTRA_NOTES).orEmpty().ifBlank {
            "Revisa tu trámite pendiente en AsistenTED."
        }
        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: title
        val openAppIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(notes)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notes))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(reminderId.hashCode(), notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Recordatorios de trámites",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos programados por el usuario para recordar trámites."
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "procedure_reminders"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_NOTES = "extra_notes"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
    }
}
