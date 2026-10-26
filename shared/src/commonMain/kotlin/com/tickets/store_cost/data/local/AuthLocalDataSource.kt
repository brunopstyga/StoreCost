package com.tickets.store_cost.data.local

import com.tickets.store_cost.data.security.Sha256
import com.tickets.store_cost.domain.model.UserSession
import org.koin.core.annotation.Single

/**
 * Fuente de datos local de autenticación.
 *
 * Guarda únicamente el hash SHA-256 de la contraseña y el flag de sesión;
 * nunca persiste la contraseña en texto plano.
 */
@Single
class AuthLocalDataSource(
    private val storage: KeyValueStorage
) {

    fun readSession(): UserSession =
        UserSession(isAuthenticated = storage.getString(KEY_LOGGED_IN) == TRUE)

    fun writeSession(session: UserSession) {
        storage.putString(KEY_LOGGED_IN, session.isAuthenticated.toString())
    }

    fun hasPassword(): Boolean = storage.getString(KEY_PASSWORD_HASH) != null

    fun savePassword(password: String) {
        storage.putString(KEY_PASSWORD_HASH, Sha256.hash(password))
    }

    fun matchesPassword(password: String): Boolean {
        val storedHash = storage.getString(KEY_PASSWORD_HASH) ?: return false
        return storedHash == Sha256.hash(password)
    }

    private companion object {
        const val KEY_PASSWORD_HASH = "auth_password_hash"
        const val KEY_LOGGED_IN = "auth_logged_in"
        const val TRUE = "true"
    }
}
