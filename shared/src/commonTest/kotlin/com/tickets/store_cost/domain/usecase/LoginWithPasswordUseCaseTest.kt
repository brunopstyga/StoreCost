package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.data.repository.FakeAuthRepository
import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LoginWithPasswordUseCaseTest {

    @Test
    fun testLoginSucceedsWithCorrectPassword() = runTest {
        val repository = FakeAuthRepository(validPassword = "clave123")
        val useCase = LoginWithPasswordUseCase(repository)

        val result = useCase("clave123")

        val success = assertIs<Result.Success<Boolean>>(result)
        assertTrue(success.data)
        assertTrue(repository.isLoggedIn)
        assertEquals(1, repository.loginCalls)
    }

    @Test
    fun testLoginFailsWithWrongPassword() = runTest {
        val repository = FakeAuthRepository(validPassword = "clave123")
        val useCase = LoginWithPasswordUseCase(repository)

        val result = useCase("incorrecta")

        val success = assertIs<Result.Success<Boolean>>(result)
        assertFalse(success.data)
        assertFalse(repository.isLoggedIn)
        assertEquals(1, repository.loginCalls)
    }

    @Test
    fun testEmptyPasswordIsRejectedWithoutCallingRepository() = runTest {
        val repository = FakeAuthRepository()
        val useCase = LoginWithPasswordUseCase(repository)

        val result = useCase("   ")

        val error = assertIs<Result.Error>(result)
        assertEquals(ErrorCode.EMPTY_PASSWORD, error.code)
        assertEquals(0, repository.loginCalls)
        assertFalse(repository.isLoggedIn)
    }

    @Test
    fun testRepositoryErrorIsPropagated() = runTest {
        val repository = FakeAuthRepository()
        repository.shouldReturnError = true
        val useCase = LoginWithPasswordUseCase(repository)

        val result = useCase(FakeAuthRepository.DEFAULT_PASSWORD)

        val error = assertIs<Result.Error>(result)
        assertEquals(ErrorCode.LOGIN_FAILED, error.code)
        assertFalse(repository.isLoggedIn)
    }
}
