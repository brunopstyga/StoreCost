package com.tickets.store_cost.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tickets.store_cost.data.local.entity.SaleEntity

@Dao
interface SaleDao {

    @Insert
    suspend fun insertSale(sale: SaleEntity)

    @Query("SELECT * FROM sales WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getSalesByDateRange(startTime: Long, endTime: Long): List<SaleEntity>
}
