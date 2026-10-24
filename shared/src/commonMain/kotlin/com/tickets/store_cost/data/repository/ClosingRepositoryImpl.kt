package com.tickets.store_cost.data.repository

import com.tickets.store_cost.data.local.dao.DailyClosingDao
import com.tickets.store_cost.data.mapper.toDomain
import com.tickets.store_cost.data.mapper.toEntity
import com.tickets.store_cost.domain.model.DailyClosing
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.repository.IClosingRepository
import org.koin.core.annotation.Single

/**
 * Implementación de [IClosingRepository] sobre Room KMP.
 */
@Single(binds = [IClosingRepository::class])
class ClosingRepositoryImpl(
    private val dailyClosingDao: DailyClosingDao
) : IClosingRepository {

    override suspend fun saveClosing(closing: DailyClosing): Result<Unit> =
        try {
            dailyClosingDao.insertClosing(closing.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Error al guardar el cierre del día: ${e.message}", e)
        }

    override suspend fun getClosingByDate(date: String): Result<DailyClosing?> =
        try {
            Result.Success(dailyClosingDao.getClosingByDate(date)?.toDomain())
        } catch (e: Exception) {
            Result.Error("Error al obtener el cierre del día: ${e.message}", e)
        }
}
