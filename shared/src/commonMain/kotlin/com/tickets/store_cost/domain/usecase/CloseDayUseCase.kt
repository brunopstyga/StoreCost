package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.DailyClosing
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.repository.IClosingRepository
import com.tickets.store_cost.domain.repository.ISaleRepository
import com.tickets.store_cost.domain.util.roundToCents
import com.tickets.store_cost.domain.util.todayIn
import com.tickets.store_cost.domain.util.toDayRange
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import org.koin.core.annotation.Factory

/**
 * Calcula y persiste el cierre del día actual.
 *
 * `calculateDailyClosing()` obtiene el total y la cantidad de ventas del día
 * (para mostrarlos en el diálogo de confirmación) sin persistir nada.
 * `invoke(closing)` guarda el cierre ya confirmado; si ya existe un cierre
 * para la misma fecha, el repositorio lo reemplaza.
 */
@Factory
class CloseDayUseCase(
    private val saleRepository: ISaleRepository,
    private val closingRepository: IClosingRepository
) {

    suspend fun calculateDailyClosing(): Result<DailyClosing> {
        val timeZone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = todayIn(timeZone)
        val (start, end) = today.toDayRange(timeZone)

        return when (val result = saleRepository.getSalesByDateRange(start, end).first()) {
            is Result.Success -> {
                val sales = result.data
                if (sales.isEmpty()) {
                    Result.Error("No hay ventas registradas para el día de hoy.")
                } else {
                    val total = sales.sumOf { it.subtotal }.roundToCents()
                    Result.Success(
                        DailyClosing(
                            closingDate = today.toString(),
                            totalAmount = total,
                            salesCount = sales.size,
                            closedAt = now.toEpochMilliseconds()
                        )
                    )
                }
            }

            is Result.Error -> result
            Result.Loading -> Result.Error("No se pudieron obtener las ventas del día.")
        }
    }

    suspend operator fun invoke(closing: DailyClosing): Result<DailyClosing> =
        when (val result = closingRepository.saveClosing(closing)) {
            is Result.Success -> Result.Success(closing)
            is Result.Error -> result
            Result.Loading -> Result.Error("No se pudo guardar el cierre del día.")
        }
}
