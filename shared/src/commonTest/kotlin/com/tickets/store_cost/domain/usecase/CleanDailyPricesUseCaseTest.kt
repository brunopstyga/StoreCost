package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.data.repository.FakeConsultingRepository
import com.tickets.store_cost.domain.model.PriceConsultation
import com.tickets.store_cost.domain.model.Result
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CleanDailyPricesUseCaseTest {

    private val now = Instant.fromEpochMilliseconds(100_000_000_000L)
    private val hourMillis = 60L * 60L * 1_000L

    private fun consultation(id: Long, timestamp: Long): PriceConsultation =
        PriceConsultation(
            id = id,
            basePrice = 1000.0,
            quantity = 1,
            selectedModality = "FACTURA_A",
            calculatedUnitPrice = 1210.0,
            calculatedSubtotal = 1210.0,
            timestamp = timestamp
        )

    @Test
    fun testDeletesOnlyConsultationsOlderThan24Hours() = runTest {
        val expired = consultation(id = 1, timestamp = now.toEpochMilliseconds() - 25 * hourMillis)
        val recent = consultation(id = 2, timestamp = now.toEpochMilliseconds() - 23 * hourMillis)
        val repository = FakeConsultingRepository(listOf(expired, recent))
        val useCase = CleanDailyPricesUseCase(repository)

        val result = useCase(now)

        assertEquals(1, assertIs<Result.Success<Int>>(result).data)
        assertEquals(listOf(recent), repository.storedConsultations)
    }

    @Test
    fun testKeepsConsultationsExactlyAtTheLimit() = runTest {
        val limitConsultation = consultation(
            id = 1,
            timestamp = now.toEpochMilliseconds() - 24 * hourMillis
        )
        val repository = FakeConsultingRepository(listOf(limitConsultation))
        val useCase = CleanDailyPricesUseCase(repository)

        val result = useCase(now)

        assertEquals(0, assertIs<Result.Success<Int>>(result).data)
        assertEquals(listOf(limitConsultation), repository.storedConsultations)
    }

    @Test
    fun testRepositoryErrorIsPropagated() = runTest {
        val repository = FakeConsultingRepository().apply { shouldReturnError = true }
        val useCase = CleanDailyPricesUseCase(repository)

        val result = useCase(now)

        assertIs<Result.Error>(result)
    }
}
