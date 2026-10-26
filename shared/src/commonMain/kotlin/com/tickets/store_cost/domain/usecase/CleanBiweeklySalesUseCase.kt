package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.SalesCleanupResult
import com.tickets.store_cost.domain.repository.IClosingRepository
import com.tickets.store_cost.domain.repository.ISaleRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.core.annotation.Factory

/**
 * Purga quincenal de **Ventas y Cierre**: elimina ventas y cierres con más de
 * 15 días de antigüedad.
 *
 * Debe ejecutarse únicamente después de generar y guardar el respaldo
 * quincenal, para no perder historial sin copia física.
 */
@Factory
class CleanBiweeklySalesUseCase(
    private val saleRepository: ISaleRepository,
    private val closingRepository: IClosingRepository
) {

    suspend operator fun invoke(now: Instant = Clock.System.now()): Result<SalesCleanupResult> {
        val limit = now.toEpochMilliseconds() - RETENTION_MILLIS

        val deletedSales = when (val result = saleRepository.deleteSalesBefore(limit)) {
            is Result.Success -> result.data
            is Result.Error -> return result
            Result.Loading -> return Result.Error(ErrorCode.DELETE_SALES_FAILED)
        }

        val deletedClosings = when (val result = closingRepository.deleteClosingsBefore(limit)) {
            is Result.Success -> result.data
            is Result.Error -> return result
            Result.Loading -> return Result.Error(ErrorCode.DELETE_CLOSINGS_FAILED)
        }

        return Result.Success(
            SalesCleanupResult(
                deletedSales = deletedSales,
                deletedClosings = deletedClosings
            )
        )
    }

    private companion object {
        const val RETENTION_MILLIS = 15L * 24L * 60L * 60L * 1_000L
    }
}
