package com.tickets.store_cost.data.repository

import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.PriceConsultation
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.repository.IConsultingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake en memoria de [IConsultingRepository] para pruebas unitarias.
 * Simula la persistencia y emite las consultas guardadas como un Flow reactivo.
 */
class FakeConsultingRepository(
    initialConsultations: List<PriceConsultation> = emptyList()
) : IConsultingRepository {

    private val consultationsFlow = MutableStateFlow(initialConsultations)
    private var nextId: Long = (initialConsultations.maxOfOrNull { it.id } ?: 0L) + 1L

    var shouldReturnError: Boolean = false

    val storedConsultations: List<PriceConsultation>
        get() = consultationsFlow.value

    override suspend fun insertConsultation(consultation: PriceConsultation): Result<Unit> {
        if (shouldReturnError) {
            return Result.Error(ErrorCode.SAVE_CONSULTATION_FAILED, "Database error")
        }
        val stored = consultation.copy(id = nextId++)
        consultationsFlow.value = consultationsFlow.value + stored
        return Result.Success(Unit)
    }

    override fun getAllConsultations(): Flow<Result<List<PriceConsultation>>> =
        consultationsFlow.map { consultations ->
            if (shouldReturnError) {
                Result.Error(ErrorCode.FETCH_CONSULTATIONS_FAILED, "Database error")
            } else {
                Result.Success(consultations)
            }
        }
}
