package com.tickets.store_cost.android.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.tickets.store_cost.android.MainActivity
import com.tickets.store_cost.android.R
import com.tickets.store_cost.domain.model.Result as DomainResult
import com.tickets.store_cost.domain.repository.IReportFileWriter
import com.tickets.store_cost.domain.usecase.GenerateMonthlyReportUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar

/**
 * Verifica diariamente si el reporte del mes anterior ya fue generado y, si no,
 * lo crea y publica una notificación para enviarlo por correo.
 */
class MonthlyReportWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val generateReport: GenerateMonthlyReportUseCase by inject()
    private val fileWriter: IReportFileWriter by inject()

    override suspend fun doWork(): ListenableWorker.Result {
        val previousMonth = previousMonth()
        val preferences = applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        if (preferences.getString(KEY_LAST_PERIOD, null) == previousMonth.key) {
            return ListenableWorker.Result.success()
        }

        val report = when (val result = generateReport(previousMonth.year, previousMonth.month)) {
            is DomainResult.Success -> result.data
            is DomainResult.Error -> return ListenableWorker.Result.failure()
            DomainResult.Loading -> return ListenableWorker.Result.retry()
        }

        val filePath = when (val result = fileWriter.write(report)) {
            is DomainResult.Success -> result.data
            is DomainResult.Error -> return ListenableWorker.Result.failure()
            DomainResult.Loading -> return ListenableWorker.Result.retry()
        }

        preferences.edit().putString(KEY_LAST_PERIOD, previousMonth.key).apply()
        notifyReportReady(filePath, previousMonth.label)
        return ListenableWorker.Result.success()
    }

    private fun previousMonth(): ReportPeriod {
        val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        return ReportPeriod(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH) + 1
        )
    }

    private fun notifyReportReady(filePath: String, periodLabel: String) {
        if (!notificationsAllowed()) return

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_REPORT_PATH, filePath)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(
                applicationContext.getString(R.string.notification_text, periodLabel)
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Se repite la verificación junto al notify() para que Android Lint
        // reconozca el permiso runtime (POST_NOTIFICATIONS) en este ámbito y no
        // reporte MissingPermission. En API < 33 el permiso no aplica.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(applicationContext)
            .notify(NOTIFICATION_ID, notification)
    }

    private fun notificationsAllowed(): Boolean {
        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            return false
        }
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private data class ReportPeriod(val year: Int, val month: Int) {
        val key: String = "${year}_${month.toString().padStart(2, '0')}"
        val label: String = "${month.toString().padStart(2, '0')}/$year"
    }

    companion object {
        const val WORK_NAME = "monthly_report_worker"
        const val CHANNEL_ID = "monthly_reports"
        private const val NOTIFICATION_ID = 2001
        private const val PREFS_NAME = "monthly_report_prefs"
        private const val KEY_LAST_PERIOD = "last_report_period"
    }
}
