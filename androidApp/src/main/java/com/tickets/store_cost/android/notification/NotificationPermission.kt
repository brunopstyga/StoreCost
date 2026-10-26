package com.tickets.store_cost.android.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Verificación centralizada de los permisos necesarios para publicar
 * notificaciones locales. En Android 13+ se requiere `POST_NOTIFICATIONS`
 * en tiempo de ejecución; además el usuario puede desactivar las
 * notificaciones de la app desde los ajustes del sistema.
 */
object NotificationPermission {

    fun isGranted(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return false
        }
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }
}
