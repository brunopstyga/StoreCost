package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.data.repository.FakeClosingRepository
import com.tickets.store_cost.data.repository.FakeSaleRepository
import com.tickets.store_cost.domain.model.DailyClosing
import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CloseDayUseCaseTest {

    private fun sampleSales(): List<Sale> = listOf(
        Sale(
            id = 1,
            basePrice = 1210.0,
            quantity = 1,
            operationType = OperationType.FACTURA_A,
            unitPrice = 1210.0,
            subtotal = 1210.0,
            timestamp = 1_000L
        ),
        Sale(
            id = 2,
            basePrice = 1350.0,
            quantity = 1,
            operationType = OperationType.FACTURA_A,
            unitPrice = 1350.0,
            subtotal = 1350.0,
            timestamp = 2_000L
        )
    )

    private fun todayIso(): String = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .toString()

    private fun buildUseCase(
        sales: List<Sale> = sampleSales(),
        closingRepository: FakeClosingRepository = FakeClosingRepository()
    ): Pair<CloseDayUseCase, FakeClosingRepository> =
        CloseDayUseCase(FakeSaleRepository(sales), closingRepository) to closingRepository

    @Test
    fun testCalculateDailyClosingReturnsTotalAndCountWithoutPersisting() = runTest {
        val (useCase, closingRepository) = buildUseCase()

        val result = useCase.calculateDailyClosing()

        val closing = assertIs<Result.Success<DailyClosing>>(result).data
        assertEquals(2560.0, closing.totalAmount, 0.001)
        assertEquals(2, closing.salesCount)
        assertEquals(todayIso(), closing.closingDate)
        assertTrue(closingRepository.savedClosings.isEmpty())
    }

    @Test
    fun testCalculateDailyClosingWithoutSalesReturnsError() = runTest {
        val (useCase, _) = buildUseCase(sales = emptyList())

        val result = useCase.calculateDailyClosing()

        assertIs<Result.Error>(result)
    }

    @Test
    fun testConfirmClosingPersistsDailyClosing() = runTest {
        val (useCase, closingRepository) = buildUseCase()
        val closing = assertIs<Result.Success<DailyClosing>>(
            useCase.calculateDailyClosing()
        ).data

        val result = useCase(closing)

        assertIs<Result.Success<DailyClosing>>(result)
        assertEquals(1, closingRepository.savedClosings.size)
        val saved = closingRepository.savedClosings.single()
        assertEquals(todayIso(), saved.closingDate)
        assertEquals(2560.0, saved.totalAmount, 0.001)
        assertEquals(2, saved.salesCount)
    }

    @Test
    fun testReclosingSameDayReplacesPreviousClosing() = runTest {
        val (useCase, closingRepository) = buildUseCase()
        val closing = assertIs<Result.Success<DailyClosing>>(
            useCase.calculateDailyClosing()
        ).data

        useCase(closing)
        useCase(closing.copy(totalAmount = 3000.0, salesCount = 3))

        assertEquals(1, closingRepository.savedClosings.size)
        assertEquals(3000.0, closingRepository.savedClosings.single().totalAmount, 0.001)
    }

    @Test
    fun testSaveClosingErrorIsPropagated() = runTest {
        val (useCase, closingRepository) = buildUseCase()
        val closing = assertIs<Result.Success<DailyClosing>>(
            useCase.calculateDailyClosing()
        ).data
        closingRepository.shouldReturnError = true

        val result = useCase(closing)

        assertIs<Result.Error>(result)
    }
}
