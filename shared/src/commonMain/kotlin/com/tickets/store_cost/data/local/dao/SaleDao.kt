package com.tickets.store_cost.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tickets.store_cost.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Insert
    suspend fun insertSale(sale: SaleEntity)

    /**
     * Flujo reactivo: Room vuelve a emitir cada vez que cambia la tabla, por
     * lo que la UI no necesita refrescar manualmente después de registrar una
     * venta o un cierre.
     */
    @Query(
        "SELECT * FROM sales WHERE timestamp BETWEEN :startTime AND :endTime " +
            "ORDER BY timestamp DESC, id DESC"
    )
    fun observeSalesByDateRange(startTime: Long, endTime: Long): Flow<List<SaleEntity>>

    /**
     * Elimina las ventas anteriores al límite indicado.
     * Devuelve la cantidad de filas borradas.
     */
    @Query("DELETE FROM sales WHERE timestamp < :limit")
    suspend fun deleteSalesBefore(limit: Long): Int
}
