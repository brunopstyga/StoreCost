package com.tickets.store_cost.domain.repository

import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import kotlinx.coroutines.flow.Flow

interface ISaleRepository {
    suspend fun registerSale(sale: Sale): Result<Unit>
    fun getSalesByDateRange(startTime: Long, endTime: Long): Flow<Result<List<Sale>>>

    /**
     * Elimina las ventas anteriores a [limit] (epoch en milisegundos) y
     * devuelve la cantidad de registros borrados.
     */
    suspend fun deleteSalesBefore(limit: Long): Result<Int>
}
