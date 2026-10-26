package com.tickets.store_cost.data.repository

import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import com.tickets.store_cost.domain.repository.ISaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSaleRepository(
    sales: List<Sale> = emptyList()
) : ISaleRepository {

    private val storedSales = sales.toMutableList()

    var lastRegisteredSale: Sale? = null
    var shouldReturnError: Boolean = false
    var shouldFailDeletion: Boolean = false

    val remainingSales: List<Sale>
        get() = storedSales.toList()

    override suspend fun registerSale(sale: Sale): Result<Unit> {
        if (shouldReturnError) {
            return Result.Error(ErrorCode.REGISTER_SALE_FAILED, "Database error")
        }
        lastRegisteredSale = sale
        return Result.Success(Unit)
    }

    override fun getSalesByDateRange(startTime: Long, endTime: Long): Flow<Result<List<Sale>>> {
        return flowOf(Result.Success(storedSales.toList()))
    }

    override suspend fun deleteSalesBefore(limit: Long): Result<Int> {
        if (shouldFailDeletion) {
            return Result.Error(ErrorCode.DELETE_SALES_FAILED, "Database error")
        }
        val expired = storedSales.filter { it.timestamp < limit }
        storedSales.removeAll(expired)
        return Result.Success(expired.size)
    }
}
