package com.tickets.store_cost.domain.model

data class Sale(
    val id: Long = 0,
    val basePrice: Double,
    val quantity: Int,
    val operationType: OperationType,
    val unitPrice: Double,
    val subtotal: Double,
    val timestamp: Long
)
