package com.tickets.store_cost.di

import com.tickets.store_cost.data.local.AppDatabase
import com.tickets.store_cost.data.local.dao.DailyClosingDao
import com.tickets.store_cost.data.local.dao.PriceConsultationDao
import com.tickets.store_cost.data.local.dao.SaleDao
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Módulo principal de Koin. [ComponentScan] descubre automáticamente las definiciones
 * anotadas del dominio, data y presentación (@Single, @Factory, @KoinViewModel).
 */
@Module
@ComponentScan("com.tickets.store_cost")
class AppModule {

    @Single
    fun provideSaleDao(appDatabase: AppDatabase): SaleDao = appDatabase.saleDao()

    @Single
    fun providePriceConsultationDao(appDatabase: AppDatabase): PriceConsultationDao =
        appDatabase.priceConsultationDao()

    @Single
    fun provideDailyClosingDao(appDatabase: AppDatabase): DailyClosingDao =
        appDatabase.dailyClosingDao()
}
