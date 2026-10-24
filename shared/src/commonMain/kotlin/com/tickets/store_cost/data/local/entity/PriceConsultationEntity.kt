package com.tickets.store_cost.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que persiste el historial de consultas de precios.
 */
@Entity(tableName = "price_consultations")
data class PriceConsultationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val basePrice: Double,
    val quantity: Int,
    val selectedModality: String,
    val calculatedUnitPrice: Double,
    val calculatedSubtotal: Double,
    val timestamp: Long
)
