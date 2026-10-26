package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.data.repository.FakeAuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CheckAuthStatusUseCaseTest {

    @Test
    fun testEmitsInitialSessionState() = runTest {
        val repository = FakeAuthRepository(initiallyLoggedIn = false)
        val useCase = CheckAuthStatusUseCase(repository)

        val initial = useCase().take(1).toList().single()

        assertFalse(initial)
    }

    @Test
    fun testEmitsLoggedInSessionState() = runTest {
        val repository = FakeAuthRepository(initiallyLoggedIn = true)
        val useCase = CheckAuthStatusUseCase(repository)

        val initial = useCase().take(1).toList().single()

        assertTrue(initial)
    }

    @Test
    fun testSessionChangesAreEmittedReactively() = runTest {
        val repository = FakeAuthRepository()
        val useCase = CheckAuthStatusUseCase(repository)

        val emissions = async(UnconfinedTestDispatcher(testScheduler)) {
            useCase().take(2).toList()
        }

        repository.login(FakeAuthRepository.DEFAULT_PASSWORD)

        assertEquals(listOf(false, true), emissions.await())
    }
}
