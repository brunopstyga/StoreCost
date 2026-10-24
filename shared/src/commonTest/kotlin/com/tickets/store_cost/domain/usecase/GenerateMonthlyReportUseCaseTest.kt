package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.data.repository.FakeSaleRepository
import com.tickets.store_cost.domain.model.MonthlyReport
import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import com.tickets.store_cost.domain.repository.ISaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GenerateMonthlyReportUseCaseTest {

    private val timeZone = TimeZone.UTC

    private fun timestamp(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        LocalDateTime(year, month, day, hour, minute).toInstant(timeZone).toEpochMilliseconds()

    private fun sale(
        id: Long,
        timestamp: Long,
        operationType: OperationType = OperationType.FACTURA_A,
        unitPrice: Double = 1210.0,
        quantity: Int = 1,
        subtotal: Double = unitPrice * quantity
    ): Sale = Sale(
        id = id,
        basePrice = 1000.0,
        quantity = quantity,
        operationType = operationType,
        unitPrice = unitPrice,
        subtotal = subtotal,
        timestamp = timestamp
    )

    @Test
    fun testGeneratesCsvWithExpectedStructureAndChronologicalOrder() = runTest {
        val sales = listOf(
            sale(
                id = 2,
                timestamp = timestamp(2024, 3, 15, 14, 30),
                operationType = OperationType.FACTURA_B,
                unitPrice = 1105.0,
                quantity = 2,
                subtotal = 2210.0
            ),
            sale(
                id = 1,
                timestamp = timestamp(2024, 3, 1, 10, 0)
            )
        )
        val useCase = GenerateMonthlyReportUseCase(FakeSaleRepository(sales))

        val result = useCase(year = 2024, month = 3, timeZone = timeZone)

        val report = assertIs<Result.Success<MonthlyReport>>(result).data
        val expected = buildString {
            appendLine("ID,Fecha,Modalidad,PrecioUnitario,Cantidad,Subtotal")
            appendLine("1,01/03/2024 10:00,FACTURA_A,1210.00,1,1210.00")
            appendLine("2,15/03/2024 14:30,FACTURA_B,1105.00,2,2210.00")
        }
        assertEquals(expected, report.content)
        assertEquals("reporte_ventas_2024_03.csv", report.fileName)
        assertEquals(2, report.salesCount)
        assertEquals(3420.0, report.totalAmount, 0.001)
    }

    @Test
    fun testEmptyMonthGeneratesHeaderOnly() = runTest {
        val useCase = GenerateMonthlyReportUseCase(FakeSaleRepository(emptyList()))

        val result = useCase(year = 2024, month = 4, timeZone = timeZone)

        val report = assertIs<Result.Success<MonthlyReport>>(result).data
        assertEquals(
            "ID,Fecha,Modalidad,PrecioUnitario,Cantidad,Subtotal\n",
            report.content
        )
        assertEquals(0, report.salesCount)
        assertEquals(0.0, report.totalAmount, 0.001)
    }

    @Test
    fun testInvalidMonthReturnsError() = runTest {
        val useCase = GenerateMonthlyReportUseCase(FakeSaleRepository(emptyList()))

        val result = useCase(year = 2024, month = 13, timeZone = timeZone)

        assertIs<Result.Error>(result)
    }

    @Test
    fun testRepositoryErrorIsPropagated() = runTest {
        val repository = object : ISaleRepository {
            override suspend fun registerSale(sale: Sale): Result<Unit> = Result.Success(Unit)

            override fun getSalesByDateRange(
                startTime: Long,
                endTime: Long
            ): Flow<Result<List<Sale>>> = flowOf(Result.Error("Base de datos no disponible"))
        }
        val useCase = GenerateMonthlyReportUseCase(repository)

        val result = useCase(year = 2024, month = 3, timeZone = timeZone)

        val error = assertIs<Result.Error>(result)
        assertEquals("Base de datos no disponible", error.message)
    }
}
