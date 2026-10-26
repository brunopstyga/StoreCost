package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.data.repository.FakeAuthRepository
import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class LogoutUseCaseTest {

    @Test
    fun testLogoutClearsSession() = runTest {
        val repository = FakeAuthRepository(initiallyLoggedIn = true)
        val useCase = LogoutUseCase(repository)

        val result = useCase()

        assertIs<Result.Success<Unit>>(result)
        assertFalse(repository.isLoggedIn)
        assertEquals(1, repository.logoutCalls)
    }

    @Test
    fun testLogoutErrorIsPropagated() = runTest {
        val repository = FakeAuthRepository(initiallyLoggedIn = true)
        repository.shouldReturnError = true
        val useCase = LogoutUseCase(repository)

        val result = useCase()

        val error = assertIs<Result.Error>(result)
        assertEquals(ErrorCode.LOGOUT_FAILED, error.code)
    }
}
