package com.tickets.store_cost.data.repository

import com.tickets.store_cost.data.local.dao.PriceConsultationDao
import com.tickets.store_cost.data.local.entity.PriceConsultationEntity
import com.tickets.store_cost.domain.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ConsultingRepositoryImplTest {

    private class FakePriceConsultationDao(
        private val consultations: List<PriceConsultationEntity> = emptyList()
    ) : PriceConsultationDao {

        var deletedConsultationId: Long? = null

        override suspend fun insertConsultation(consultation: PriceConsultationEntity) = Unit

        override fun getAllConsultations(): Flow<List<PriceConsultationEntity>> =
            flowOf(consultations)

        override suspend fun deleteConsultationById(id: Long): Int {
            deletedConsultationId = id
            return 1
        }

        override suspend fun deleteConsultationsBefore(limit: Long): Int = 0
    }

    private fun consultationEntity(): PriceConsultationEntity = PriceConsultationEntity(
        id = 1L,
        basePrice = 1000.0,
        quantity = 1,
        selectedModality = "FACTURA_A",
        calculatedUnitPrice = 1210.0,
        calculatedSubtotal = 1210.0,
        timestamp = 1_000L
    )

    @Test
    fun testDeleteConsultationDelegatesToDao() = runTest {
        val dao = FakePriceConsultationDao(listOf(consultationEntity()))
        val repository = ConsultingRepositoryImpl(dao)

        val result = repository.deleteConsultation(7L)

        assertIs<Result.Success<Unit>>(result)
        assertEquals(7L, dao.deletedConsultationId)
    }
}
