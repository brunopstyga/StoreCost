package com.tickets.store_cost.domain.model

/**
 * Registro histórico de una consulta de precios realizada por el usuario.
 */
data class PriceConsultation(
    val id: Long = 0,
    val basePrice: Double,
    val quantity: Int,
    val selectedModality: String,
    val calculatedUnitPrice: Double,
    val calculatedSubtotal: Double,
    val timestamp: Long
)
