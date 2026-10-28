package com.tickets.store_cost.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.tickets.store_cost.presentation.screen.components.AppOutlinedTextField
import com.tickets.store_cost.presentation.screen.components.ErrorMessage
import com.tickets.store_cost.presentation.state.LoginUiState
import com.tickets.store_cost.presentation.theme.AppStyles
import com.tickets.store_cost.presentation.util.displayMessage
import com.tickets.store_cost.resources.Res
import com.tickets.store_cost.resources.login_button
import com.tickets.store_cost.resources.login_hide_password
import com.tickets.store_cost.resources.login_loading
import com.tickets.store_cost.resources.login_password_label
import com.tickets.store_cost.resources.login_show_password
import com.tickets.store_cost.resources.login_subtitle
import com.tickets.store_cost.resources.login_title
import org.jetbrains.compose.resources.stringResource

/**
 * Pantalla de Login desacoplada del ViewModel. Recibe el estado y las acciones
 * desde el contenedor raíz, que es quien decide si mostrar Login o la app.
 */
@Composable
fun LoginScreen(
    state: LoginUiState,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLogin: () -> Unit
) {
    val visualTransformation = remember(state.isPasswordVisible) {
        if (state.isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        }
    }
    val passwordKeyboard = remember {
        KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(AppStyles.Spacing.xl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppStyles.Spacing.md)
        ) {
            Text(
                text = stringResource(Res.string.login_title),
                style = AppStyles.Typography.appTitle
            )
            Text(
                text = stringResource(Res.string.login_subtitle),
                style = AppStyles.Typography.body,
                color = AppStyles.Colors.secondaryText
            )

            Spacer(modifier = Modifier.height(AppStyles.Spacing.lg))

            AppOutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChanged,
                label = stringResource(Res.string.login_password_label),
                enabled = !state.isLoading,
                visualTransformation = visualTransformation,
                keyboardOptions = passwordKeyboard,
                keyboardActions = KeyboardActions(onDone = { onLogin() }),
                trailingIcon = {
                    TextButton(
                        onClick = onTogglePasswordVisibility,
                        enabled = !state.isLoading
                    ) {
                        Text(
                            text = stringResource(
                                if (state.isPasswordVisible) {
                                    Res.string.login_hide_password
                                } else {
                                    Res.string.login_show_password
                                }
                            )
                        )
                    }
                }
            )

            state.error?.let { error ->
                ErrorMessage(
                    message = error.displayMessage(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = onLogin,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(AppStyles.Spacing.sm))
                }
                Text(
                    text = stringResource(
                        if (state.isLoading) Res.string.login_loading else Res.string.login_button
                    )
                )
            }
        }
    }
}
