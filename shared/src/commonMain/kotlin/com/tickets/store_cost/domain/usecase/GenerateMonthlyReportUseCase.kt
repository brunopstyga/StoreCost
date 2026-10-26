package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.MonthlyReport
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.repository.ISaleRepository
import com.tickets.store_cost.domain.util.buildSalesCsv
import com.tickets.store_cost.domain.util.roundToCents
import com.tickets.store_cost.domain.util.toMonthRange
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.koin.core.annotation.Factory

/**
 * Genera el reporte CSV de las ventas de un mes.
 *
 * Columnas: `ID`, `Fecha`, `Modalidad`, `PrecioUnitario`, `Cantidad`, `Subtotal`.
 * Las ventas se ordenan cronológicamente y los importes usan punto decimal.
 */
@Factory
class GenerateMonthlyReportUseCase(
    private val saleRepository: ISaleRepository
) {

    suspend operator fun invoke(
        year: Int,
        month: Int,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Result<MonthlyReport> {
        if (month !in 1..12) {
            return Result.Error(ErrorCode.INVALID_MONTH)
        }

        val (startTime, endTime) = LocalDate(year, month, 1).toMonthRange(timeZone)

        return when (val salesResult = saleRepository.getSalesByDateRange(startTime, endTime).first()) {
            is Result.Success -> {
                val sales = salesResult.data.sortedBy { it.timestamp }
                Result.Success(
                    MonthlyReport(
                        fileName = "reporte_ventas_${year}_${month.toString().padStart(2, '0')}.csv",
                        content = buildSalesCsv(sales, timeZone),
                        salesCount = sales.size,
                        totalAmount = sales.sumOf { it.subtotal }.roundToCents()
                    )
                )
            }

            is Result.Error -> salesResult
            Result.Loading -> Result.Error(ErrorCode.FETCH_MONTHLY_SALES_FAILED)
        }
    }
}
