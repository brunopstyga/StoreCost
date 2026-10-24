package com.tickets.store_cost.domain.model

/**
 * Cierre diario de ventas. Existe como máximo un registro por día
 * (clave `closingDate` en formato ISO `yyyy-MM-dd`).
 */
data class DailyClosing(
    val id: Long = 0,
    val closingDate: String,
    val totalAmount: Double,
    val salesCount: Int,
    val closedAt: Long
)
