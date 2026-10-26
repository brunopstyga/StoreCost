package com.tickets.store_cost.data.repository

import com.tickets.store_cost.data.local.AuthLocalDataSource
import com.tickets.store_cost.data.local.KeyValueStorage
import com.tickets.store_cost.domain.model.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthRepositoryImplTest {

    private class FakeKeyValueStorage : KeyValueStorage {
        private val values = mutableMapOf<String, String>()

        override fun getString(key: String): String? = values[key]

        override fun putString(key: String, value: String) {
            values[key] = value
        }

        override fun remove(key: String) {
            values.remove(key)
        }
    }

    private fun buildRepository(): AuthRepositoryImpl =
        AuthRepositoryImpl(AuthLocalDataSource(FakeKeyValueStorage()))

    private fun buildRepositoryWithStoredPassword(password: String): AuthRepositoryImpl {
        val dataSource = AuthLocalDataSource(FakeKeyValueStorage())
        dataSource.savePassword(password)
        return AuthRepositoryImpl(dataSource)
    }

    @Test
    fun testNewInstallUsesDefaultPassword1234() = runTest {
        val repository = buildRepository()

        val result = repository.login("1234")

        assertTrue(assertIs<Result.Success<Boolean>>(result).data)
    }

    @Test
    fun testLegacyDefaultPasswordIsMigratedTo1234() = runTest {
        val repository = buildRepositoryWithStoredPassword("storecost")

        val newPasswordResult = repository.login("1234")
        val legacyPasswordResult = repository.login("storecost")

        assertTrue(assertIs<Result.Success<Boolean>>(newPasswordResult).data)
        assertFalse(assertIs<Result.Success<Boolean>>(legacyPasswordResult).data)
    }

    @Test
    fun testCustomPasswordIsNotReplacedDuringMigration() = runTest {
        val repository = buildRepositoryWithStoredPassword("mi-clave")

        val defaultPasswordResult = repository.login("1234")
        val customPasswordResult = repository.login("mi-clave")

        assertFalse(assertIs<Result.Success<Boolean>>(defaultPasswordResult).data)
        assertTrue(assertIs<Result.Success<Boolean>>(customPasswordResult).data)
    }
}
