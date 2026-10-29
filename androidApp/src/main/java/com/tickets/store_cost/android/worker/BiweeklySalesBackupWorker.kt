package com.tickets.store_cost.android.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.tickets.store_cost.android.R
import com.tickets.store_cost.android.notification.ReportNotificationSender
import com.tickets.store_cost.core.logging.AppLogger
import com.tickets.store_cost.domain.model.Result as DomainResult
import com.tickets.store_cost.domain.repository.IReportFileWriter
import com.tickets.store_cost.domain.usecase.CleanBiweeklySalesUseCase
import com.tickets.store_cost.domain.usecase.GenerateBiweeklySalesBackupUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Proceso quincenal de **Ventas y Cierre**:
 *
 * 1. Genera el respaldo CSV con todo el historial de ventas y cierres.
 * 2. Guarda el archivo en el almacenamiento de la app.
 * 3. Notifica al usuario (solo si tiene permisos de notificación).
 * 4. Purga de Room las ventas y cierres con más de 15 días.
 *
 * El respaldo se genera **antes** de la limpieza para no perder información.
 */
class BiweeklySalesBackupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val generateBackup: GenerateBiweeklySalesBackupUseCase by inject()
    private val fileWriter: IReportFileWriter by inject()
    private val cleanBiweeklySales: CleanBiweeklySalesUseCase by inject()

    override suspend fun doWork(): ListenableWorker.Result {
        val backup = when (val result = generateBackup()) {
            is DomainResult.Success -> result.data
            is DomainResult.Error -> {
                AppLogger.e("Respaldo quincenal fallido (${result.code.name}): ${result.message}")
                return ListenableWorker.Result.failure()
            }
            DomainResult.Loading -> return ListenableWorker.Result.retry()
        }

        val filePath = when (val result = fileWriter.write(backup.fileName, backup.content)) {
            is DomainResult.Success -> result.data
            is DomainResult.Error -> {
                AppLogger.e(
                    "No se pudo guardar el respaldo quincenal (${result.code.name}): ${result.message}"
                )
                return ListenableWorker.Result.failure()
            }
            DomainResult.Loading -> return ListenableWorker.Result.retry()
        }

        notifyBackupReady(filePath, backup.salesCount, backup.closingsCount)

        return when (val result = cleanBiweeklySales()) {
            is DomainResult.Success -> ListenableWorker.Result.success()
            is DomainResult.Error -> {
                AppLogger.e("Purga quincenal fallida (${result.code.name}): ${result.message}")
                ListenableWorker.Result.failure()
            }
            DomainResult.Loading -> ListenableWorker.Result.retry()
        }
    }

    private fun notifyBackupReady(filePath: String, salesCount: Int, closingsCount: Int) {
        ReportNotificationSender.sendReportReady(
            context = applicationContext,
            channelId = CHANNEL_ID,
            notificationId = NOTIFICATION_ID,
            title = applicationContext.getString(R.string.biweekly_notification_title),
            text = applicationContext.getString(
                R.string.biweekly_notification_text,
                salesCount,
                closingsCount
            ),
            reportPath = filePath
        )
    }

    companion object {
        const val WORK_NAME = "biweekly_sales_backup_worker"
        const val CHANNEL_ID = "biweekly_reports"
        private const val NOTIFICATION_ID = 2002
    }
}
