package com.tickets.store_cost.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tickets.store_cost.data.local.entity.PriceConsultationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceConsultationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsultation(consultation: PriceConsultationEntity)

    @Query("SELECT * FROM price_consultations ORDER BY timestamp DESC")
    fun getAllConsultations(): Flow<List<PriceConsultationEntity>>

    /**
     * Elimina las consultas de precios anteriores al límite indicado.
     * Devuelve la cantidad de filas borradas.
     */
    @Query("DELETE FROM price_consultations WHERE timestamp < :limit")
    suspend fun deleteConsultationsBefore(limit: Long): Int
}
