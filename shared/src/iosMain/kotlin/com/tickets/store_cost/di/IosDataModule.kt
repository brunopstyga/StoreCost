package com.tickets.store_cost.di

import com.tickets.store_cost.data.local.KeyValueStorage
import com.tickets.store_cost.data.local.UserDefaultsKeyValueStorage
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Módulo de Koin específico de iOS para el almacenamiento local de
 * credenciales/sesión. Debe registrarse cuando la app iOS inicialice Koin.
 */
@Module
class IosDataModule {

    @Single
    fun provideKeyValueStorage(): KeyValueStorage = UserDefaultsKeyValueStorage()
}
