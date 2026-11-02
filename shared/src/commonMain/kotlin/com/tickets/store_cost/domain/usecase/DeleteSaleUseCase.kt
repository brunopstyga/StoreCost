package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.repository.ISaleRepository
import org.koin.core.annotation.Factory

/**
 * Elimina una venta puntual del historial de transacciones.
 * La confirmación previa es responsabilidad de la capa de presentación.
 */
@Factory
class DeleteSaleUseCase(
    private val repository: ISaleRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> =
        repository.deleteSale(id)
}
