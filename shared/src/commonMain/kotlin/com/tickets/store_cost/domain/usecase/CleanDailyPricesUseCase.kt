package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.repository.IConsultingRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.core.annotation.Factory

/**
 * Purga diaria de las consultas de precios de la pantalla **Gestión de Precios**.
 *
 * Conserva únicamente los registros de las últimas 24 horas. Las ventas y sus
 * cierres no se ven afectados por esta política.
 */
@Factory
class CleanDailyPricesUseCase(
    private val repository: IConsultingRepository
) {

    suspend operator fun invoke(now: Instant = Clock.System.now()): Result<Int> {
        val limit = now.toEpochMilliseconds() - RETENTION_MILLIS
        return repository.deleteConsultationsBefore(limit)
    }

    private companion object {
        const val RETENTION_MILLIS = 24L * 60L * 60L * 1_000L
    }
}
