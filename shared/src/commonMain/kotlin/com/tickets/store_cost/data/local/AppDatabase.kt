package com.tickets.store_cost.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.tickets.store_cost.data.local.dao.DailyClosingDao
import com.tickets.store_cost.data.local.dao.PriceConsultationDao
import com.tickets.store_cost.data.local.dao.SaleDao
import com.tickets.store_cost.data.local.entity.DailyClosingEntity
import com.tickets.store_cost.data.local.entity.PriceConsultationEntity
import com.tickets.store_cost.data.local.entity.SaleEntity

@Database(
    entities = [SaleEntity::class, PriceConsultationEntity::class, DailyClosingEntity::class],
    version = 3,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun saleDao(): SaleDao
    abstract fun priceConsultationDao(): PriceConsultationDao
    abstract fun dailyClosingDao(): DailyClosingDao
}

/**
 * Constructor de la base de datos para KMP. Room (KSP) genera la implementación
 * `actual` de este `expect object` para cada plataforma (Android e iOS).
 *
 * Requerido por Room KMP en `commonMain`: sin `@ConstructedBy` la compilación
 * para targets no-Android falla con:
 * "The @Database class must be annotated with @ConstructedBy ...".
 */
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
