package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.data.repository.FakeClosingRepository
import com.tickets.store_cost.data.repository.FakeSaleRepository
import com.tickets.store_cost.domain.model.DailyClosing
import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import com.tickets.store_cost.domain.model.SalesCleanupResult
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CleanBiweeklySalesUseCaseTest {

    private val now = Instant.fromEpochMilliseconds(100_000_000_000L)
    private val dayMillis = 24L * 60L * 60L * 1_000L

    private fun sale(id: Long, timestamp: Long): Sale = Sale(
        id = id,
        basePrice = 1000.0,
        quantity = 1,
        operationType = OperationType.FACTURA_A,
        unitPrice = 1210.0,
        subtotal = 1210.0,
        timestamp = timestamp
    )

    private fun closing(id: Long, date: String, closedAt: Long): DailyClosing =
        DailyClosing(
            id = id,
            closingDate = date,
            totalAmount = 1210.0,
            salesCount = 1,
            closedAt = closedAt
        )

    @Test
    fun testDeletesSalesAndClosingsOlderThan15Days() = runTest {
        val expiredSale = sale(id = 1, timestamp = now.toEpochMilliseconds() - 16 * dayMillis)
        val recentSale = sale(id = 2, timestamp = now.toEpochMilliseconds() - 14 * dayMillis)
        val expiredClosing = closing(
            id = 1,
            date = "2024-01-01",
            closedAt = now.toEpochMilliseconds() - 16 * dayMillis
        )
        val recentClosing = closing(
            id = 2,
            date = "2024-01-02",
            closedAt = now.toEpochMilliseconds() - 14 * dayMillis
        )
        val saleRepository = FakeSaleRepository(listOf(expiredSale, recentSale))
        val closingRepository = FakeClosingRepository(listOf(expiredClosing, recentClosing))
        val useCase = CleanBiweeklySalesUseCase(saleRepository, closingRepository)

        val result = useCase(now)

        val cleanup = assertIs<Result.Success<SalesCleanupResult>>(result).data
        assertEquals(1, cleanup.deletedSales)
        assertEquals(1, cleanup.deletedClosings)
        assertEquals(listOf(recentSale), saleRepository.remainingSales)
        assertEquals(listOf(recentClosing), closingRepository.savedClosings)
    }

    @Test
    fun testKeepsRecordsExactlyAtTheLimit() = runTest {
        val limitSale = sale(id = 1, timestamp = now.toEpochMilliseconds() - 15 * dayMillis)
        val limitClosing = closing(
            id = 1,
            date = "2024-01-01",
            closedAt = now.toEpochMilliseconds() - 15 * dayMillis
        )
        val saleRepository = FakeSaleRepository(listOf(limitSale))
        val closingRepository = FakeClosingRepository(listOf(limitClosing))
        val useCase = CleanBiweeklySalesUseCase(saleRepository, closingRepository)

        val result = useCase(now)

        val cleanup = assertIs<Result.Success<SalesCleanupResult>>(result).data
        assertEquals(0, cleanup.deletedSales)
        assertEquals(0, cleanup.deletedClosings)
        assertEquals(listOf(limitSale), saleRepository.remainingSales)
        assertEquals(listOf(limitClosing), closingRepository.savedClosings)
    }

    @Test
    fun testSaleDeletionErrorIsPropagated() = runTest {
        val saleRepository = FakeSaleRepository().apply { shouldFailDeletion = true }
        val useCase = CleanBiweeklySalesUseCase(saleRepository, FakeClosingRepository())

        val result = useCase(now)

        assertIs<Result.Error>(result)
    }

    @Test
    fun testClosingDeletionErrorIsPropagated() = runTest {
        val closingRepository = FakeClosingRepository().apply { shouldFailDeletion = true }
        val useCase = CleanBiweeklySalesUseCase(FakeSaleRepository(), closingRepository)

        val result = useCase(now)

        assertIs<Result.Error>(result)
    }
}
