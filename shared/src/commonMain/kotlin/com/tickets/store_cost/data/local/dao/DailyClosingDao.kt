package com.tickets.store_cost.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tickets.store_cost.data.local.entity.DailyClosingEntity

@Dao
interface DailyClosingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClosing(closing: DailyClosingEntity)

    @Query("SELECT * FROM daily_closings WHERE closingDate = :date LIMIT 1")
    suspend fun getClosingByDate(date: String): DailyClosingEntity?

    @Query("SELECT * FROM daily_closings ORDER BY closedAt ASC")
    suspend fun getAllClosings(): List<DailyClosingEntity>

    /**
     * Elimina los cierres anteriores al límite indicado.
     * Devuelve la cantidad de filas borradas.
     */
    @Query("DELETE FROM daily_closings WHERE closedAt < :limit")
    suspend fun deleteClosingsBefore(limit: Long): Int
}
