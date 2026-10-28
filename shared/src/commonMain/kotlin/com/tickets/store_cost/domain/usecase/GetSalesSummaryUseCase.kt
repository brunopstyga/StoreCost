package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import com.tickets.store_cost.domain.model.SalesSummary
import com.tickets.store_cost.domain.repository.ISaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
class GetSalesSummaryUseCase(
    private val repository: ISaleRepository
) {

    suspend operator fun invoke(
        startTime: Long,
        endTime: Long
    ): Result<SalesSummary> = getSummary(startTime, endTime)

    /**
     * Expone el resumen del período como flujo frío para que la capa de presentación
     * lo transforme en un [kotlinx.coroutines.flow.StateFlow] mediante `stateIn`.
     */
    fun observeSalesSummary(
        startTime: Long,
        endTime: Long
    ): Flow<Result<SalesSummary>> =
        repository.getSalesByDateRange(startTime, endTime).map { result ->
            when (result) {
                is Result.Success -> Result.Success(buildSummary(result.data))
                is Result.Error -> result
                is Result.Loading -> Result.Loading
            }
        }

    suspend fun getMonthlyClosing(
        startOfMonth: Long,
        endOfMonth: Long
    ): Result<SalesSummary> = getSummary(startOfMonth, endOfMonth)

    private suspend fun getSummary(startTime: Long, endTime: Long): Result<SalesSummary> {
        val salesResult: Result<List<Sale>> =
            repository.getSalesByDateRange(startTime, endTime).first()

        return when (salesResult) {
            is Result.Success -> Result.Success(buildSummary(salesResult.data))
            is Result.Error -> salesResult
            else -> Result.Error(ErrorCode.FETCH_SALES_FAILED)
        }
    }

    private fun buildSummary(sales: List<Sale>): SalesSummary = SalesSummary(
        totalBilled = sales.sumOf { it.subtotal },
        totalSalesCount = sales.size,
        breakdown = sales.groupBy { it.operationType }
            .mapValues { (_, salesList) -> salesList.sumOf { it.subtotal } },
        sales = sales
    )
}
