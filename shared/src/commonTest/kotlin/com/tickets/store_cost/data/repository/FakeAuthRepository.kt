package com.tickets.store_cost.data.repository

import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake en memoria de [IAuthRepository] para pruebas unitarias.
 *
 * Simula una contraseña válida y una sesión reactiva, de modo que los casos de
 * uso y el ViewModel puedan probarse sin depender del almacenamiento real.
 */
class FakeAuthRepository(
    validPassword: String = DEFAULT_PASSWORD,
    initiallyLoggedIn: Boolean = false
) : IAuthRepository {

    private val session = MutableStateFlow(initiallyLoggedIn)

    var validPassword: String = validPassword
        private set

    var shouldReturnError: Boolean = false
    var loginCalls: Int = 0
        private set
    var logoutCalls: Int = 0
        private set

    val isLoggedIn: Boolean
        get() = session.value

    override suspend fun login(password: String): Result<Boolean> {
        loginCalls += 1
        if (shouldReturnError) {
            return Result.Error(ErrorCode.LOGIN_FAILED, "Auth error")
        }
        val matches = password == validPassword
        if (matches) {
            session.value = true
        }
        return Result.Success(matches)
    }

    override fun observeIsLoggedIn(): Flow<Boolean> = session.asStateFlow()

    override suspend fun logout(): Result<Unit> {
        logoutCalls += 1
        if (shouldReturnError) {
            return Result.Error(ErrorCode.LOGOUT_FAILED, "Auth error")
        }
        session.value = false
        return Result.Success(Unit)
    }

    override suspend fun saveInitialPassword(password: String): Result<Unit> {
        if (shouldReturnError) {
            return Result.Error(ErrorCode.SAVE_PASSWORD_FAILED, "Auth error")
        }
        validPassword = password
        return Result.Success(Unit)
    }

    companion object {
        const val DEFAULT_PASSWORD: String = "1234"
    }
}
