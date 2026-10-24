package com.tickets.store_cost.domain.repository

import com.tickets.store_cost.domain.model.DailyClosing
import com.tickets.store_cost.domain.model.Result

/**
 * Contrato del repositorio de cierres diarios.
 */
interface IClosingRepository {
    suspend fun saveClosing(closing: DailyClosing): Result<Unit>

    suspend fun getClosingByDate(date: String): Result<DailyClosing?>
}
