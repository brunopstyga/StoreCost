package com.tickets.store_cost.domain.model

enum class OperationType(val surchargeRate: Double) {
    FACTURA_A(0.21),
    FACTURA_B(0.105),
    LOTE_100(1.0),
    LOTE_110(1.1),
    EFECTIVO_NEGRO(0.35);

    fun calculateUnitPrice(basePrice: Double): Double {
        return basePrice * (1.0 + surchargeRate)
    }
}
