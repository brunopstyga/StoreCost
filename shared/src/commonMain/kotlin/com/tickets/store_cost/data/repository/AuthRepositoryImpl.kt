package com.tickets.store_cost.data.repository

import com.tickets.store_cost.data.local.AuthLocalDataSource
import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.UserSession
import com.tickets.store_cost.domain.model.resultOf
import com.tickets.store_cost.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

/**
 * Implementación de [IAuthRepository] sobre almacenamiento clave-valor local.
 *
 * La contraseña se compara por hash y la sesión se mantiene en un
 * [MutableStateFlow] para que la UI reaccione sin volver a leer el storage.
 */
@Single(binds = [IAuthRepository::class])
class AuthRepositoryImpl(
    private val localDataSource: AuthLocalDataSource
) : IAuthRepository {

    private val session = MutableStateFlow(localDataSource.readSession().isAuthenticated)

    override suspend fun login(password: String): Result<Boolean> =
        resultOf(ErrorCode.LOGIN_FAILED) {
            ensureInitialPassword()
            val matches = localDataSource.matchesPassword(password)
            if (matches) {
                localDataSource.writeSession(UserSession(isAuthenticated = true))
                session.value = true
            }
            matches
        }

    override fun observeIsLoggedIn(): Flow<Boolean> = session.asStateFlow()

    override suspend fun logout(): Result<Unit> =
        resultOf(ErrorCode.LOGOUT_FAILED) {
            localDataSource.writeSession(UserSession(isAuthenticated = false))
            session.value = false
        }

    override suspend fun saveInitialPassword(password: String): Result<Unit> =
        resultOf(ErrorCode.SAVE_PASSWORD_FAILED) {
            localDataSource.savePassword(password)
        }

    /**
     * Siembra una contraseña inicial mientras la app no tenga una pantalla de
     * configuración de credenciales. Una vez guardada, el usuario puede
     * reemplazarla a través de [saveInitialPassword].
     */
    private fun ensureInitialPassword() {
        if (!localDataSource.hasPassword()) {
            localDataSource.savePassword(DEFAULT_INITIAL_PASSWORD)
        }
    }

    companion object {
        /**
         * Contraseña inicial documentada para el primer acceso.
         */
        const val DEFAULT_INITIAL_PASSWORD: String = "storecost"
    }
}
