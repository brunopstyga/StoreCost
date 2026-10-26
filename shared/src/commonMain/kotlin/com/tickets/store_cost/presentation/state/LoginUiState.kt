package com.tickets.store_cost.presentation.state

import com.tickets.store_cost.domain.model.Result

/**
 * Estado inmutable de la pantalla de Login.
 *
 * [error] conserva el error de dominio para que la UI lo traduzca a un texto
 * localizado; [errorMessage] queda disponible para logs y pruebas.
 */
data class LoginUiState(
    val password: String = "",
    val isLoading: Boolean = false,
    val error: Result.Error? = null,
    val isAuthenticated: Boolean = false,
    val isPasswordVisible: Boolean = false
) {
    val errorMessage: String?
        get() = error?.message
}
