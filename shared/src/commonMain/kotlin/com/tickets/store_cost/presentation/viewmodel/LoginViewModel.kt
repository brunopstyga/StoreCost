package com.tickets.store_cost.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.usecase.CheckAuthStatusUseCase
import com.tickets.store_cost.domain.usecase.LoginWithPasswordUseCase
import com.tickets.store_cost.domain.usecase.LogoutUseCase
import com.tickets.store_cost.presentation.state.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

/**
 * ViewModel UDF de la autenticación local.
 *
 * Combina el formulario de contraseña con el flujo reactivo de sesión y
 * expone un único [StateFlow] cálido de [LoginUiState].
 */
@KoinViewModel
class LoginViewModel(
    private val loginWithPasswordUseCase: LoginWithPasswordUseCase,
    private val checkAuthStatusUseCase: CheckAuthStatusUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private data class FormState(
        val password: String = "",
        val isLoading: Boolean = false,
        val error: Result.Error? = null,
        val isPasswordVisible: Boolean = false
    )

    private val formState = MutableStateFlow(FormState())

    val uiState: StateFlow<LoginUiState> =
        combine(formState, checkAuthStatusUseCase()) { form, isAuthenticated ->
            LoginUiState(
                password = form.password,
                isLoading = form.isLoading,
                error = form.error,
                isAuthenticated = isAuthenticated,
                isPasswordVisible = form.isPasswordVisible
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LoginUiState()
        )

    fun onPasswordChanged(newPassword: String) {
        formState.update { it.copy(password = newPassword, error = null) }
    }

    fun onTogglePasswordVisibility() {
        formState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginClicked() {
        if (formState.value.isLoading) return

        val password = formState.value.password
        viewModelScope.launch {
            formState.update { it.copy(isLoading = true, error = null) }

            when (val result = loginWithPasswordUseCase(password)) {
                is Result.Success -> {
                    if (result.data) {
                        formState.update {
                            it.copy(isLoading = false, password = "", error = null)
                        }
                    } else {
                        formState.update {
                            it.copy(
                                isLoading = false,
                                error = Result.Error(ErrorCode.INVALID_PASSWORD)
                            )
                        }
                    }
                }
                is Result.Error -> formState.update {
                    it.copy(isLoading = false, error = result)
                }
                Result.Loading -> formState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            logoutUseCase()
            formState.update {
                it.copy(password = "", error = null, isLoading = false)
            }
        }
    }
}
