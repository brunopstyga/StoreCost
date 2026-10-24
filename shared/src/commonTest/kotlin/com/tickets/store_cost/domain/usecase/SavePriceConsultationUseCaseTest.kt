package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.data.repository.FakeConsultingRepository
import com.tickets.store_cost.domain.model.PriceConsultation
import com.tickets.store_cost.domain.model.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SavePriceConsultationUseCaseTest {

    private fun sampleConsultation() = PriceConsultation(
        basePrice = 1000.0,
        quantity = 2,
        selectedModality = "FACTURA_A",
        calculatedUnitPrice = 1210.0,
        calculatedSubtotal = 2420.0,
        timestamp = 1_000L
    )

    @Test
    fun testSaveConsultationPersistsThroughRepository() = runTest {
        val repository = FakeConsultingRepository()
        val useCase = SavePriceConsultationUseCase(repository)

        val result = useCase(sampleConsultation())

        assertIs<Result.Success<Unit>>(result)
        assertEquals(1, repository.storedConsultations.size)
        val stored = repository.storedConsultations.first()
        assertEquals(1000.0, stored.basePrice, 0.001)
        assertEquals(2, stored.quantity)
        assertEquals("FACTURA_A", stored.selectedModality)
        assertEquals(1210.0, stored.calculatedUnitPrice, 0.001)
        assertEquals(2420.0, stored.calculatedSubtotal, 0.001)
    }

    @Test
    fun testSaveConsultationPropagatesRepositoryError() = runTest {
        val repository = FakeConsultingRepository()
        repository.shouldReturnError = true
        val useCase = SavePriceConsultationUseCase(repository)

        val result = useCase(sampleConsultation())

        assertIs<Result.Error>(result)
        assertTrue(repository.storedConsultations.isEmpty())
    }
}
