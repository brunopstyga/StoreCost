package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.data.repository.FakeClosingRepository
import com.tickets.store_cost.data.repository.FakeSaleRepository
import com.tickets.store_cost.domain.model.DailyClosing
import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import com.tickets.store_cost.domain.model.SalesBackup
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GenerateBiweeklySalesBackupUseCaseTest {

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
    fun testBackupIncludesFullSalesAndClosingsHistory() = runTest {
        val sales = listOf(
            sale(id = 1, timestamp = timestamp(2024, 3, 1, 10, 0)),
            sale(
                id = 2,
                timestamp = timestamp(2024, 3, 15, 14, 30),
                operationType = OperationType.FACTURA_B,
                unitPrice = 1105.0,
                quantity = 2,
                subtotal = 2210.0
            )
        )
        val closings = listOf(
            DailyClosing(
                id = 1,
                closingDate = "2024-03-15",
                totalAmount = 2210.0,
                salesCount = 1,
                closedAt = timestamp(2024, 3, 15, 20, 0)
            )
        )
        val useCase = GenerateBiweeklySalesBackupUseCase(
            saleRepository = FakeSaleRepository(sales),
            closingRepository = FakeClosingRepository(closings)
        )
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp(2024, 3, 16, 0, 0))

        val result = useCase(now, timeZone)

        val backup = assertIs<Result.Success<SalesBackup>>(result).data
        val expected = buildString {
            appendLine("# VENTAS")
            appendLine("ID,Fecha,Modalidad,PrecioUnitario,Cantidad,Subtotal")
            appendLine("1,01/03/2024 10:00,FACTURA_A,1210.00,1,1210.00")
            appendLine("2,15/03/2024 14:30,FACTURA_B,1105.00,2,2210.00")
            appendLine()
            appendLine("# CIERRES DIARIOS")
            appendLine("ID,Fecha,Total,Operaciones,CerradoEn")
            appendLine("1,2024-03-15,2210.00,1,15/03/2024 20:00")
        }
        assertEquals(expected, backup.content)
        assertEquals("respaldo_ventas_quincenal_2024-03-16.csv", backup.fileName)
        assertEquals(2, backup.salesCount)
        assertEquals(1, backup.closingsCount)
        assertEquals(3420.0, backup.totalAmount, 0.001)
    }

    @Test
    fun testEmptyHistoryGeneratesHeadersOnly() = runTest {
        val useCase = GenerateBiweeklySalesBackupUseCase(
            saleRepository = FakeSaleRepository(),
            closingRepository = FakeClosingRepository()
        )
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp(2024, 3, 16, 0, 0))

        val result = useCase(now, timeZone)

        val backup = assertIs<Result.Success<SalesBackup>>(result).data
        assertEquals(
            "# VENTAS\n" +
                "ID,Fecha,Modalidad,PrecioUnitario,Cantidad,Subtotal\n" +
                "\n" +
                "# CIERRES DIARIOS\n" +
                "ID,Fecha,Total,Operaciones,CerradoEn\n",
            backup.content
        )
        assertEquals(0, backup.salesCount)
        assertEquals(0, backup.closingsCount)
        assertEquals(0.0, backup.totalAmount, 0.001)
    }
}
