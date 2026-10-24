package com.tickets.store_cost.domain.model

data class PriceCalculationResult(
    val operationType: OperationType,
    val unitPrice: Double,
    val subtotal: Double
)
