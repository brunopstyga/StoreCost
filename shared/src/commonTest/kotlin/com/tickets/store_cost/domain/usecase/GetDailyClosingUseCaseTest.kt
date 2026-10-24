package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.data.repository.FakeClosingRepository
import com.tickets.store_cost.domain.model.DailyClosing
import com.tickets.store_cost.domain.model.Result
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class GetDailyClosingUseCaseTest {

    private fun todayIso(): String = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .toString()

    @Test
    fun testReturnsTodayClosingWhenExists() = runTest {
        val closing = DailyClosing(
            id = 1,
            closingDate = todayIso(),
            totalAmount = 2560.0,
            salesCount = 2,
            closedAt = 1_000L
        )
        val useCase = GetDailyClosingUseCase(FakeClosingRepository(listOf(closing)))

        val result = useCase()

        val saved = assertIs<Result.Success<DailyClosing?>>(result).data
        assertEquals(2560.0, saved?.totalAmount ?: 0.0, 0.001)
        assertEquals(2, saved?.salesCount)
    }

    @Test
    fun testReturnsNullWhenThereIsNoClosing() = runTest {
        val useCase = GetDailyClosingUseCase(FakeClosingRepository())

        val result = useCase()

        assertIs<Result.Success<DailyClosing?>>(result)
        assertNull(result.data)
    }

    @Test
    fun testReturnsClosingForExplicitDate() = runTest {
        val closing = DailyClosing(
            id = 1,
            closingDate = "2026-01-15",
            totalAmount = 500.0,
            salesCount = 1,
            closedAt = 1_000L
        )
        val useCase = GetDailyClosingUseCase(FakeClosingRepository(listOf(closing)))

        val result = useCase("2026-01-15")

        val saved = assertIs<Result.Success<DailyClosing?>>(result).data
        assertEquals(500.0, saved?.totalAmount ?: 0.0, 0.001)
    }
}
