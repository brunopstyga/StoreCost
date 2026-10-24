package com.tickets.store_cost.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.tickets.store_cost.android.R
import com.tickets.store_cost.android.worker.MonthlyReportWorker
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.com_tickets_store_cost_di_AppModule
import org.koin.ksp.generated.com_tickets_store_cost_di_DatabaseModule
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Punto de entrada de Android que inicializa Koin, crea el canal de
 * notificaciones y programa la verificación periódica del reporte mensual.
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
        createNotificationChannel()
        scheduleMonthlyReportWorker()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MonthlyReportWorker.CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
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
