package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.SalesBackup
import com.tickets.store_cost.domain.repository.IClosingRepository
import com.tickets.store_cost.domain.repository.ISaleRepository
import com.tickets.store_cost.domain.util.buildClosingsCsv
import com.tickets.store_cost.domain.util.buildSalesCsv
import com.tickets.store_cost.domain.util.roundToCents
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory

/**
 * Genera el respaldo quincenal antes de purgar los datos antiguos.
 *
 * Incluye **todo** el historial disponible de ventas y cierres: la limpieza de
 * 15 días se ejecuta después de que este archivo fue escrito en disco, de modo
 * que ningún registro se pierde sin respaldo.
 */
@Factory
class GenerateBiweeklySalesBackupUseCase(
    private val saleRepository: ISaleRepository,
    private val closingRepository: IClosingRepository
) {

    suspend operator fun invoke(
        now: Instant = Clock.System.now(),
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Result<SalesBackup> {
        val sales = when (val result = saleRepository.getSalesByDateRange(
            startTime = Long.MIN_VALUE,
            endTime = Long.MAX_VALUE
        ).first()) {
            is Result.Success -> result.data.sortedBy { it.timestamp }
            is Result.Error -> return result
            Result.Loading -> return Result.Error(ErrorCode.FETCH_SALES_FAILED)
        }

        val closings = when (val result = closingRepository.getAllClosings()) {
            is Result.Success -> result.data.sortedBy { it.closedAt }
            is Result.Error -> return result
            Result.Loading -> return Result.Error(ErrorCode.FETCH_CLOSING_FAILED)
        }

        val dateLabel = now.toLocalDateTime(timeZone).date.toString()
        val content = buildString {
            appendLine(SALES_SECTION_HEADER)
            append(buildSalesCsv(sales, timeZone))
            appendLine()
            appendLine(CLOSINGS_SECTION_HEADER)
            append(buildClosingsCsv(closings, timeZone))
        }

        return Result.Success(
            SalesBackup(
                fileName = "respaldo_ventas_quincenal_$dateLabel.csv",
                content = content,
                salesCount = sales.size,
                closingsCount = closings.size,
                totalAmount = sales.sumOf { it.subtotal }.roundToCents()
            )
        )
    }

    private companion object {
        const val SALES_SECTION_HEADER = "# VENTAS"
        const val CLOSINGS_SECTION_HEADER = "# CIERRES DIARIOS"
    }
}
