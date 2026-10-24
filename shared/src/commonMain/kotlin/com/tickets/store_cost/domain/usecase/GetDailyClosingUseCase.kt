package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.DailyClosing
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.repository.IClosingRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory

/**
 * Obtiene el cierre guardado para una fecha (por defecto, el día actual).
 */
@Factory
class GetDailyClosingUseCase(
    private val repository: IClosingRepository
) {
    suspend operator fun invoke(date: String? = null): Result<DailyClosing?> {
        val targetDate = date ?: Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString()
        return repository.getClosingByDate(targetDate)
    }
}
