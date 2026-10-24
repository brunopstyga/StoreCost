package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.data.repository.FakeConsultingRepository
import com.tickets.store_cost.domain.model.PriceConsultation
import com.tickets.store_cost.domain.model.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetPriceConsultationsUseCaseTest {

    private fun consultation(
        id: Long,
        modality: String,
        unitPrice: Double,
        subtotal: Double
    ) = PriceConsultation(
        id = id,
        basePrice = 1000.0,
        quantity = 2,
        selectedModality = modality,
        calculatedUnitPrice = unitPrice,
        calculatedSubtotal = subtotal,
        timestamp = id * 1_000L
    )

    @Test
    fun testGetConsultationsEmitsStoredHistory() = runTest {
        val repository = FakeConsultingRepository(
            listOf(
                consultation(1, "FACTURA_A", 1210.0, 2420.0),
                consultation(2, "EFECTIVO_NEGRO", 1350.0, 2700.0)
            )
        )
        val useCase = GetPriceConsultationsUseCase(repository)

        val result = useCase().first()

        val success = assertIs<Result.Success<List<PriceConsultation>>>(result)
        assertEquals(2, success.data.size)
        assertEquals("FACTURA_A", success.data[0].selectedModality)
        assertEquals(2420.0, success.data[0].calculatedSubtotal, 0.001)
        assertEquals("EFECTIVO_NEGRO", success.data[1].selectedModality)
        assertEquals(2700.0, success.data[1].calculatedSubtotal, 0.001)
    }

    @Test
    fun testGetConsultationsPropagatesRepositoryError() = runTest {
        val repository = FakeConsultingRepository()
        repository.shouldReturnError = true
        val useCase = GetPriceConsultationsUseCase(repository)

        val result = useCase().first()

        assertIs<Result.Error>(result)
    }
}
