package com.tickets.store_cost.data.repository

import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import com.tickets.store_cost.domain.repository.ISaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSaleRepository(
    private val sales: List<Sale> = emptyList()
) : ISaleRepository {

    var lastRegisteredSale: Sale? = null
    var shouldReturnError: Boolean = false

    override suspend fun registerSale(sale: Sale): Result<Unit> {
        if (shouldReturnError) {
            return Result.Error(ErrorCode.REGISTER_SALE_FAILED, "Database error")
        }
        lastRegisteredSale = sale
        return Result.Success(Unit)
    }

    override fun getSalesByDateRange(startTime: Long, endTime: Long): Flow<Result<List<Sale>>> {
        return flowOf(Result.Success(sales))
    }
}
