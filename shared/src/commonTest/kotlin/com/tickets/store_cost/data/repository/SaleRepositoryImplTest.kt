package com.tickets.store_cost.data.repository

import com.tickets.store_cost.data.local.dao.SaleDao
import com.tickets.store_cost.data.local.entity.SaleEntity
import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SaleRepositoryImplTest {

    private class FakeSaleDao(
        private val sales: List<SaleEntity> = emptyList(),
        private val failure: Exception? = null,
        private val gate: CompletableDeferred<Unit>? = null
    ) : SaleDao {

        override suspend fun insertSale(sale: SaleEntity) = Unit

        override suspend fun getSalesByDateRange(
            startTime: Long,
            endTime: Long
        ): List<SaleEntity> {
            gate?.await()
            failure?.let { throw it }
            return sales
        }
    }

    private fun saleEntity(): SaleEntity = SaleEntity(
        id = 1,
        basePrice = 1000.0,
        quantity = 1,
        operationType = OperationType.FACTURA_A.name,
        unitPrice = 1210.0,
        subtotal = 1210.0,
        timestamp = 1_000L
    )

    @Test
    fun testEmitsMappedSales() = runTest {
        val repository = SaleRepositoryImpl(FakeSaleDao(listOf(saleEntity())))

        val result = repository.getSalesByDateRange(0L, Long.MAX_VALUE).first()

        val sales = assertIs<Result.Success<List<Sale>>>(result).data
        assertEquals(1, sales.size)
        assertEquals(1210.0, sales.single().subtotal, 0.001)
    }

    @Test
    fun testDaoFailureEmitsErrorResult() = runTest {
        val repository = SaleRepositoryImpl(
            FakeSaleDao(failure = IllegalStateException("boom"))
        )

        val result = repository.getSalesByDateRange(0L, Long.MAX_VALUE).first()

        val error = assertIs<Result.Error>(result)
        assertTrue(error.message.contains("boom"))
    }

    /**
     * Regresión: al cancelarse la colección (p. ej. `flatMapLatest` al cambiar
     * de pestaña) el flujo no debe intentar emitir un `Result.Error`, ya que
     * eso viola la transparencia de excepciones de Flow.
     */
    @Test
    fun testCancelledCollectionDoesNotEmitError() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = SaleRepositoryImpl(FakeSaleDao(gate = gate))
        val received = mutableListOf<Result<List<Sale>>>()

        val job = launch {
            repository.getSalesByDateRange(0L, Long.MAX_VALUE).collect { received += it }
        }
        runCurrent()

        job.cancel()
        gate.complete(Unit)
        job.join()

        assertTrue(received.isEmpty())
    }
}
