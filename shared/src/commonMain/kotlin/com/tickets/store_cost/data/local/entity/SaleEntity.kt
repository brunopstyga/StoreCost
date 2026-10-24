package com.tickets.store_cost.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val basePrice: Double,
    val quantity: Int,
    val operationType: String,
    val unitPrice: Double,
    val subtotal: Double,
    val timestamp: Long
)
