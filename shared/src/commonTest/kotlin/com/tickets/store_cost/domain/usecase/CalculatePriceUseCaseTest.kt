package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.OperationType
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculatePriceUseCaseTest {

    private val calculatePriceUseCase = CalculatePriceUseCase()

    @Test
    fun testFacturaAPrice() {
        val base = 1000.0
        val result = calculatePriceUseCase(base, OperationType.FACTURA_A)
        assertEquals(1210.0, result, 0.001)
    }

    @Test
    fun testFacturaBPrice() {
        val base = 1000.0
        val result = calculatePriceUseCase(base, OperationType.FACTURA_B)
        assertEquals(1105.0, result, 0.001)
    }

    @Test
    fun testLote100Price() {
        val base = 1000.0
        val result = calculatePriceUseCase(base, OperationType.LOTE_100)
        assertEquals(2000.0, result, 0.001)
    }

    @Test
    fun testLote110Price() {
        val base = 1000.0
        val result = calculatePriceUseCase(base, OperationType.LOTE_110)
        assertEquals(2100.0, result, 0.001)
    }

    @Test
    fun testEfectivoNegroPrice() {
        val base = 1000.0
        val result = calculatePriceUseCase(base, OperationType.EFECTIVO_NEGRO)
        assertEquals(1350.0, result, 0.001)
    }
}
