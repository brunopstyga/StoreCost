package com.tickets.store_cost.android.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.tickets.store_cost.android.MainActivity

/**
 * Publica notificaciones de reportes listos de forma segura: si el usuario no
 * concedió el permiso (o desactivó las notificaciones) no se intenta publicar
 * nada, evitando excepciones en tiempo de ejecución.
 */
object ReportNotificationSender {

    fun sendReportReady(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        text: String,
        reportPath: String
    ) {
        if (!NotificationPermission.isGranted(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_REPORT_PATH, reportPath)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Se repite la verificación junto al notify() para que Android Lint
        // reconozca el permiso runtime (POST_NOTIFICATIONS) en este ámbito y no
        // reporte MissingPermission. En API < 33 el permiso no aplica.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
