package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.repository.IConsultingRepository
import org.koin.core.annotation.Factory

/**
 * Elimina una consulta de precio puntual del historial calculado.
 * La confirmación previa es responsabilidad de la capa de presentación.
 */
@Factory
class DeletePriceConsultationUseCase(
    private val repository: IConsultingRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> =
        repository.deleteConsultation(id)
}
