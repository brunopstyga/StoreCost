package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import com.tickets.store_cost.domain.model.SalesSummary
import com.tickets.store_cost.domain.repository.ISaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetSalesSummaryUseCaseTest {

    private class FakeSaleRepository(private val sales: List<Sale>) : ISaleRepository {
        override suspend fun registerSale(sale: Sale): Result<Unit> = Result.Success(Unit)

        override fun getSalesByDateRange(startTime: Long, endTime: Long): Flow<Result<List<Sale>>> {
            return flowOf(Result.Success(sales))
        }

        override suspend fun deleteSalesBefore(limit: Long): Result<Int> = Result.Success(0)
    }

    @Test
    fun testDailyClosing() = runTest {
        val sale1 = Sale(id = 1, basePrice = 1000.0, quantity = 1, operationType = OperationType.FACTURA_A, unitPrice = 1210.0, subtotal = 1210.0, timestamp = 1710000000000L)
        val sale2 = Sale(id = 2, basePrice = 1000.0, quantity = 1, operationType = OperationType.EFECTIVO_NEGRO, unitPrice = 1350.0, subtotal = 1350.0, timestamp = 1710000000001L)
        val repository = FakeSaleRepository(listOf(sale1, sale2))
        val useCase = GetSalesSummaryUseCase(repository)

        val result = useCase(startTime = 0L, endTime = Long.MAX_VALUE)

        assertTrue(result is Result.Success)
        val summary = result.data
        assertEquals(2560.0, summary.totalBilled, 0.001)
        assertEquals(2, summary.totalSalesCount)
        assertEquals(1210.0, summary.breakdown[OperationType.FACTURA_A] ?: 0.0, 0.001)
        assertEquals(1350.0, summary.breakdown[OperationType.EFECTIVO_NEGRO] ?: 0.0, 0.001)
    }

    @Test
    fun testHistoryClosingAccumulationWithFullRange() = runTest {
        val sale1 = Sale(id = 1, basePrice = 1000.0, quantity = 1, operationType = OperationType.FACTURA_A, unitPrice = 1210.0, subtotal = 1210.0, timestamp = 1710000000000L)
        val sale2 = Sale(id = 2, basePrice = 1000.0, quantity = 1, operationType = OperationType.EFECTIVO_NEGRO, unitPrice = 1350.0, subtotal = 1350.0, timestamp = 1710086400000L)
        val sale3 = Sale(id = 3, basePrice = 1000.0, quantity = 1, operationType = OperationType.FACTURA_B, unitPrice = 1105.0, subtotal = 1105.0, timestamp = 1710172800000L)
        val repository = FakeSaleRepository(listOf(sale1, sale2, sale3))
        val useCase = GetSalesSummaryUseCase(repository)

        val result = useCase(startTime = Long.MIN_VALUE, endTime = Long.MAX_VALUE)

        assertTrue(result is Result.Success)
        val summary = result.data
        assertEquals(3665.0, summary.totalBilled, 0.001)
        assertEquals(3, summary.totalSalesCount)
        assertEquals(1210.0, summary.breakdown[OperationType.FACTURA_A] ?: 0.0, 0.001)
        assertEquals(1350.0, summary.breakdown[OperationType.EFECTIVO_NEGRO] ?: 0.0, 0.001)
        assertEquals(1105.0, summary.breakdown[OperationType.FACTURA_B] ?: 0.0, 0.001)
    }

    @Test
    fun testMonthlyClosingAccumulation() = runTest {
        val sale1 = Sale(id = 1, basePrice = 1000.0, quantity = 1, operationType = OperationType.FACTURA_A, unitPrice = 1210.0, subtotal = 1210.0, timestamp = 1710000000000L)
        val sale2 = Sale(id = 2, basePrice = 1000.0, quantity = 1, operationType = OperationType.EFECTIVO_NEGRO, unitPrice = 1350.0, subtotal = 1350.0, timestamp = 1710086400000L)
        val sale3 = Sale(id = 3, basePrice = 1000.0, quantity = 1, operationType = OperationType.FACTURA_B, unitPrice = 1105.0, subtotal = 1105.0, timestamp = 1710172800000L)
        val sale4 = Sale(id = 4, basePrice = 1000.0, quantity = 1, operationType = OperationType.LOTE_100, unitPrice = 2000.0, subtotal = 2000.0, timestamp = 1710264000000L)
        val repository = FakeSaleRepository(listOf(sale1, sale2, sale3, sale4))
        val useCase = GetSalesSummaryUseCase(repository)

        val result = useCase.getMonthlyClosing(sale1.timestamp, sale4.timestamp)

        assertTrue(result is Result.Success)
        val summary = result.data
        assertEquals(5665.0, summary.totalBilled, 0.001)
        assertEquals(4, summary.totalSalesCount)
        assertEquals(1210.0, summary.breakdown[OperationType.FACTURA_A] ?: 0.0, 0.001)
        assertEquals(1350.0, summary.breakdown[OperationType.EFECTIVO_NEGRO] ?: 0.0, 0.001)
        assertEquals(1105.0, summary.breakdown[OperationType.FACTURA_B] ?: 0.0, 0.001)
        assertEquals(2000.0, summary.breakdown[OperationType.LOTE_100] ?: 0.0, 0.001)
    }
}
