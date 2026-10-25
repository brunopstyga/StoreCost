package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.core.util.formatDateTime
import com.tickets.store_cost.domain.model.MonthlyReport
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import com.tickets.store_cost.domain.repository.ISaleRepository
import com.tickets.store_cost.domain.util.roundToCents
import com.tickets.store_cost.domain.util.toMonthRange
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.math.abs
import kotlin.math.roundToLong
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
            return Result.Error("El mes debe estar entre 1 y 12.")
        }

        val (startTime, endTime) = LocalDate(year, month, 1).toMonthRange(timeZone)

        return when (val salesResult = saleRepository.getSalesByDateRange(startTime, endTime).first()) {
            is Result.Success -> {
                val sales = salesResult.data.sortedBy { it.timestamp }
                Result.Success(
                    MonthlyReport(
                        fileName = "reporte_ventas_${year}_${month.toString().padStart(2, '0')}.csv",
                        content = buildCsv(sales, timeZone),
                        salesCount = sales.size,
                        totalAmount = sales.sumOf { it.subtotal }.roundToCents()
                    )
                )
            }

            is Result.Error -> salesResult
            Result.Loading -> Result.Error("No se pudieron obtener las ventas del mes.")
        }
    }

    private fun buildCsv(sales: List<Sale>, timeZone: TimeZone): String = buildString {
        appendLine(CSV_HEADER)
        sales.forEach { sale ->
            appendLine(
                listOf(
                    sale.id.toString(),
                    formatDateTime(sale.timestamp, timeZone),
                    escapeCsv(sale.operationType.name),
                    formatDecimal(sale.unitPrice),
                    sale.quantity.toString(),
                    formatDecimal(sale.subtotal)
                ).joinToString(",")
            )
        }
    }

    private fun formatDecimal(value: Double): String {
        val cents = (value * 100).roundToLong()
        val sign = if (cents < 0) "-" else ""
        val absolute = abs(cents)
        return "$sign${absolute / 100}.${(absolute % 100).toString().padStart(2, '0')}"
    }

    private fun escapeCsv(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }

    private companion object {
        const val CSV_HEADER = "ID,Fecha,Modalidad,PrecioUnitario,Cantidad,Subtotal"
    }
}
