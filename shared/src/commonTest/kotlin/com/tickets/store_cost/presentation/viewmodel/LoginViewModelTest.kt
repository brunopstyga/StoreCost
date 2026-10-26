package com.tickets.store_cost.presentation.viewmodel

import com.tickets.store_cost.data.repository.FakeAuthRepository
import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.repository.IAuthRepository
import com.tickets.store_cost.domain.usecase.CheckAuthStatusUseCase
import com.tickets.store_cost.domain.usecase.LoginWithPasswordUseCase
import com.tickets.store_cost.domain.usecase.LogoutUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeAuthRepository(validPassword = "clave123")
        viewModel = buildViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(repo: IAuthRepository): LoginViewModel =
        LoginViewModel(
            loginWithPasswordUseCase = LoginWithPasswordUseCase(repo),
            checkAuthStatusUseCase = CheckAuthStatusUseCase(repo),
            logoutUseCase = LogoutUseCase(repo)
        )

    @Test
    fun testInitialState() {
        val state = viewModel.uiState.value

        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertFalse(state.isAuthenticated)
        assertFalse(state.isPasswordVisible)
        assertNull(state.error)
    }

    @Test
    fun testPasswordChangeUpdatesState() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onPasswordChanged("clave123")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("clave123", state.password)
        assertNull(state.error)
    }

    @Test
    fun testTogglePasswordVisibility() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onTogglePasswordVisibility()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isPasswordVisible)

        viewModel.onTogglePasswordVisibility()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isPasswordVisible)
    }

    @Test
    fun testLoginWithCorrectPasswordAuthenticates() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onPasswordChanged("clave123")
        viewModel.onLoginClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isAuthenticated)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("", state.password)
    }

    @Test
    fun testLoginWithWrongPasswordShowsError() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onPasswordChanged("incorrecta")
        viewModel.onLoginClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertFalse(state.isLoading)
        assertEquals(ErrorCode.INVALID_PASSWORD, state.error?.code)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun testLoginWithEmptyPasswordShowsErrorWithoutCallingRepository() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onLoginClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertEquals(ErrorCode.EMPTY_PASSWORD, state.error?.code)
        assertEquals(0, repository.loginCalls)
    }

    @Test
    fun testRepositoryErrorIsShown() = runTest(dispatcher) {
        repository.shouldReturnError = true
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onPasswordChanged("clave123")
        viewModel.onLoginClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertEquals(ErrorCode.LOGIN_FAILED, state.error?.code)
    }

    @Test
    fun testLogoutClearsSession() = runTest(dispatcher) {
        val loggedInRepository = FakeAuthRepository(initiallyLoggedIn = true)
        val loggedInViewModel = buildViewModel(loggedInRepository)
        backgroundScope.launch { loggedInViewModel.uiState.collect {} }
        advanceUntilIdle()
        assertTrue(loggedInViewModel.uiState.value.isAuthenticated)

        loggedInViewModel.onLogoutClicked()
        advanceUntilIdle()

        val state = loggedInViewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertEquals("", state.password)
        assertEquals(1, loggedInRepository.logoutCalls)
    }

    @Test
    fun testErrorIsClearedWhenPasswordChanges() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onLoginClicked()
        advanceUntilIdle()
        assertEquals(ErrorCode.EMPTY_PASSWORD, viewModel.uiState.value.error?.code)

        viewModel.onPasswordChanged("c")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
    }
}
