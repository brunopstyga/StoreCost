package com.tickets.store_cost.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room del cierre diario. El índice único garantiza un solo
 * cierre por fecha (`closingDate` en formato ISO `yyyy-MM-dd`).
 */
@Entity(
    tableName = "daily_closings",
    indices = [Index(value = ["closingDate"], unique = true)]
)
data class DailyClosingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val closingDate: String,
    val totalAmount: Double,
    val salesCount: Int,
    val closedAt: Long
)
