package com.tickets.store_cost.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tickets.store_cost.presentation.viewmodel.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Raíz de la aplicación. Observa el estado reactivo de sesión y decide entre
 * la pantalla de Login y el contenedor principal autenticado.
 */
@Composable
fun StoreCostApp(
    onSendReport: (String) -> Unit = {},
    onShareLogs: () -> Unit = {},
    loginViewModel: LoginViewModel = koinViewModel()
) {
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()

    if (loginState.isAuthenticated) {
        MainContent(
            onSendReport = onSendReport,
            onShareLogs = onShareLogs,
            onLogout = loginViewModel::onLogoutClicked
        )
    } else {
        LoginScreen(
            state = loginState,
            onPasswordChanged = loginViewModel::onPasswordChanged,
            onTogglePasswordVisibility = loginViewModel::onTogglePasswordVisibility,
            onLogin = loginViewModel::onLoginClicked
        )
    }
}
