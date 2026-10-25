package com.tickets.store_cost.data.repository

import com.tickets.store_cost.data.local.dao.PriceConsultationDao
import com.tickets.store_cost.data.local.entity.PriceConsultationEntity
import com.tickets.store_cost.data.mapper.toDomain
import com.tickets.store_cost.data.mapper.toEntity
import com.tickets.store_cost.domain.model.PriceConsultation
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.resultOf
import com.tickets.store_cost.domain.repository.IConsultingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.coroutines.cancellation.CancellationException

/**
 * Implementación de [IConsultingRepository] sobre Room KMP.
 */
@Single(binds = [IConsultingRepository::class])
class ConsultingRepositoryImpl(
    private val priceConsultationDao: PriceConsultationDao
) : IConsultingRepository {

    override suspend fun insertConsultation(consultation: PriceConsultation): Result<Unit> =
        resultOf("Error al guardar la consulta") {
            priceConsultationDao.insertConsultation(consultation.toEntity())
            Unit
        }

    override fun getAllConsultations(): Flow<Result<List<PriceConsultation>>> =
        priceConsultationDao.getAllConsultations()
            .map<List<PriceConsultationEntity>, Result<List<PriceConsultation>>> { entities ->
                Result.Success(entities.map { it.toDomain() })
            }
            .catch { e ->
                // La cancelación debe propagarse: no es un error de negocio.
                if (e is CancellationException) throw e
                emit(Result.Error("Error al obtener el historial de consultas: ${e.message}", e))
            }
}
