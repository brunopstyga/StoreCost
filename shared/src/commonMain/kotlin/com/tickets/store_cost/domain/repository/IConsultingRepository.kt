package com.tickets.store_cost.domain.repository

import com.tickets.store_cost.domain.model.PriceConsultation
import com.tickets.store_cost.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de consultas de precios.
 * La capa de dominio desconoce los detalles de persistencia (Room).
 */
interface IConsultingRepository {
    suspend fun insertConsultation(consultation: PriceConsultation): Result<Unit>

    fun getAllConsultations(): Flow<Result<List<PriceConsultation>>>
}
