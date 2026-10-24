package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.PriceConsultation
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.repository.IConsultingRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

/**
 * Expone el flujo frío con el historial de consultas de precios.
 */
@Factory
class GetPriceConsultationsUseCase(
    private val repository: IConsultingRepository
) {
    operator fun invoke(): Flow<Result<List<PriceConsultation>>> =
        repository.getAllConsultations()
}
