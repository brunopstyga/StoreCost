package com.tickets.store_cost.data.repository

import com.tickets.store_cost.data.local.dao.DailyClosingDao
import com.tickets.store_cost.data.mapper.toDomain
import com.tickets.store_cost.data.mapper.toEntity
import com.tickets.store_cost.domain.model.DailyClosing
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.resultOf
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
        resultOf("Error al guardar el cierre del día") {
            dailyClosingDao.insertClosing(closing.toEntity())
            Unit
        }

    override suspend fun getClosingByDate(date: String): Result<DailyClosing?> =
        resultOf("Error al obtener el cierre del día") {
            dailyClosingDao.getClosingByDate(date)?.toDomain()
        }
}
