package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.data.repository.FakeAuthRepository
import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.getOrNull
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SavePasswordUseCaseTest {

    @Test
    fun testSaveInitialPasswordReplacesStoredPassword() = runTest {
        val repository = FakeAuthRepository(validPassword = "anterior")
        val useCase = SavePasswordUseCase(repository)

        val result = useCase("nueva")

        assertIs<Result.Success<Unit>>(result)
        assertTrue(repository.login("nueva").getOrNull() == true)
    }

    @Test
    fun testEmptyPasswordIsRejected() = runTest {
        val repository = FakeAuthRepository()
        val useCase = SavePasswordUseCase(repository)

        val result = useCase("")

        val error = assertIs<Result.Error>(result)
        assertEquals(ErrorCode.EMPTY_PASSWORD, error.code)
    }

    @Test
    fun testRepositoryErrorIsPropagated() = runTest {
        val repository = FakeAuthRepository()
        repository.shouldReturnError = true
        val useCase = SavePasswordUseCase(repository)

        val result = useCase("nueva")

        val error = assertIs<Result.Error>(result)
        assertEquals(ErrorCode.SAVE_PASSWORD_FAILED, error.code)
    }
}
