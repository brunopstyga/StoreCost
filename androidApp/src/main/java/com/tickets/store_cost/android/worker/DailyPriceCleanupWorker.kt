package com.tickets.store_cost.android.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.tickets.store_cost.core.logging.AppLogger
import com.tickets.store_cost.domain.model.Result as DomainResult
import com.tickets.store_cost.domain.usecase.CleanDailyPricesUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Purga diaria de las consultas de precios temporales (pantalla
 * **Gestión de Precios**). Conserva las últimas 24 horas.
 */
class DailyPriceCleanupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val cleanDailyPrices: CleanDailyPricesUseCase by inject()

    override suspend fun doWork(): Result =
        when (val result = cleanDailyPrices()) {
            is DomainResult.Success -> Result.success()
            is DomainResult.Error -> {
                AppLogger.e("Purga diaria de precios fallida (${result.code.name}): ${result.message}")
                Result.retry()
            }
            DomainResult.Loading -> Result.retry()
        }

    companion object {
        const val WORK_NAME = "daily_price_cleanup_worker"
    }
}
