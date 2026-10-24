package com.tickets.store_cost.di

import android.content.Context
import androidx.room.Room
import com.tickets.store_cost.data.local.AppDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Módulo de Koin específico de Android que provee la instancia única de Room.
 * Requiere que la aplicación registre el [Context] al iniciar Koin (androidContext()).
 */
@Module
class DatabaseModule {

    @Single
    fun provideAppDatabase(context: Context): AppDatabase =
        Room.databaseBuilder<AppDatabase>(context, "store_cost.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
}
