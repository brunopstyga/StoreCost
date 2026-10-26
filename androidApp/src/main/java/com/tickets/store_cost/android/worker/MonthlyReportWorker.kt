package com.tickets.store_cost.android.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.tickets.store_cost.android.R
import com.tickets.store_cost.android.notification.ReportNotificationSender
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
        ReportNotificationSender.sendReportReady(
            context = applicationContext,
            channelId = CHANNEL_ID,
            notificationId = NOTIFICATION_ID,
            title = applicationContext.getString(R.string.notification_title),
            text = applicationContext.getString(R.string.notification_text, periodLabel),
            reportPath = filePath
        )
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
