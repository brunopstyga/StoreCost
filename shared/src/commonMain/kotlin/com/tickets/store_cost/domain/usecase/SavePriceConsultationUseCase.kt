package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.PriceConsultation
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.repository.IConsultingRepository
import org.koin.core.annotation.Factory

/**
 * Persiste una consulta de precios en el repositorio.
 */
@Factory
class SavePriceConsultationUseCase(
    private val repository: IConsultingRepository
) {
    suspend operator fun invoke(consultation: PriceConsultation): Result<Unit> =
        repository.insertConsultation(consultation)
}
