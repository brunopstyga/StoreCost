package com.tickets.store_cost.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.tickets.store_cost.android.R
import com.tickets.store_cost.android.worker.BiweeklySalesBackupWorker
import com.tickets.store_cost.android.worker.DailyPriceCleanupWorker
import com.tickets.store_cost.android.worker.MonthlyReportWorker
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.com_tickets_store_cost_di_AppModule
import org.koin.ksp.generated.com_tickets_store_cost_di_DatabaseModule
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Punto de entrada de Android que inicializa Koin, crea los canales de
 * notificaciones y programa las tareas periódicas de WorkManager:
 *
 * - **Diaria**: purga de consultas de precios de más de 24 horas.
 * - **Quincenal**: respaldo de ventas/cierres y purga de más de 15 días.
 * - **Mensual**: generación del reporte mensual (Tarea 03).
 */
class StoreCostApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@StoreCostApplication)
            modules(
                com_tickets_store_cost_di_AppModule,
                com_tickets_store_cost_di_DatabaseModule
            )
        }
        createNotificationChannels()
        scheduleMonthlyReportWorker()
        scheduleDailyPriceCleanupWorker()
        scheduleBiweeklySalesBackupWorker()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    MonthlyReportWorker.CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = getString(R.string.notification_channel_description)
                }
            )
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    BiweeklySalesBackupWorker.CHANNEL_ID,
                    getString(R.string.biweekly_notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = getString(R.string.biweekly_notification_channel_description)
                }
            )
        }
    }

    private fun scheduleMonthlyReportWorker() {
        val request = PeriodicWorkRequestBuilder<MonthlyReportWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayUntilNextMonth(), TimeUnit.MILLISECONDS)
            .addTag(MonthlyReportWorker.WORK_NAME)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            MonthlyReportWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Purga diaria de consultas de precios. Usa [ExistingPeriodicWorkPolicy.UPDATE]
     * para que los cambios de restricciones se apliquen en actualizaciones.
     */
    private fun scheduleDailyPriceCleanupWorker() {
        val request = PeriodicWorkRequestBuilder<DailyPriceCleanupWorker>(1, TimeUnit.DAYS)
            .setConstraints(backgroundConstraints())
            .addTag(DailyPriceCleanupWorker.WORK_NAME)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            DailyPriceCleanupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /**
     * Respaldo + purga quincenal de ventas y cierres.
     */
    private fun scheduleBiweeklySalesBackupWorker() {
        val request = PeriodicWorkRequestBuilder<BiweeklySalesBackupWorker>(15, TimeUnit.DAYS)
            .setConstraints(backgroundConstraints())
            .addTag(BiweeklySalesBackupWorker.WORK_NAME)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            BiweeklySalesBackupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun backgroundConstraints(): Constraints =
        Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

    /**
     * Primera ejecución: el día 1 del próximo mes a las 00:05. A partir de ahí
     * el worker corre a diario y genera el reporte pendiente una sola vez.
     */
    private fun initialDelayUntilNextMonth(): Long {
        val now = Calendar.getInstance()
        val target = (now.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 5)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return (target.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
    }
}
