package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import com.tickets.store_cost.domain.repository.ISaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegisterSaleUseCaseTest {

    private class FakeSaleRepository : ISaleRepository {
        var lastRegisteredSale: Sale? = null
        var shouldReturnError = false

        override suspend fun registerSale(sale: Sale): Result<Unit> {
            if (shouldReturnError) {
                return Result.Error("Database error")
            }
            lastRegisteredSale = sale
            return Result.Success(Unit)
        }

        override fun getSalesByDateRange(startTime: Long, endTime: Long): Flow<Result<List<Sale>>> {
            return flowOf(Result.Success(emptyList()))
        }
    }

    @Test
    fun testRegisterSaleSubtotalCalculation() = runTest {
        val repository = FakeSaleRepository()
        val calculatePriceUseCase = CalculatePriceUseCase()
        val registerSaleUseCase = RegisterSaleUseCase(repository, calculatePriceUseCase)

        val result = registerSaleUseCase(
            basePrice = 1000.0,
            quantity = 2,
            operationType = OperationType.FACTURA_A
        )

        assertTrue(result is Result.Success)
        val registeredSale = repository.lastRegisteredSale
        assertEquals(1210.0, registeredSale?.unitPrice ?: 0.0, 0.001)
        assertEquals(2420.0, registeredSale?.subtotal ?: 0.0, 0.001)
        assertEquals(2, registeredSale?.quantity)
    }

    @Test
    fun testRegisterSaleErrorResult() = runTest {
        val repository = FakeSaleRepository()
        repository.shouldReturnError = true
        val registerSaleUseCase = RegisterSaleUseCase(repository, CalculatePriceUseCase())

        val result = registerSaleUseCase(
            basePrice = 1000.0,
            quantity = 2,
            operationType = OperationType.FACTURA_A
        )

        assertTrue(result is Result.Error)
    }

    @Test
    fun testRegisterSaleInvalidBasePrice() = runTest {
        val repository = FakeSaleRepository()
        val registerSaleUseCase = RegisterSaleUseCase(repository, CalculatePriceUseCase())

        val result = registerSaleUseCase(
            basePrice = 0.0,
            quantity = 2,
            operationType = OperationType.FACTURA_A
        )

        assertTrue(result is Result.Error)
    }
}
