package com.tickets.store_cost.presentation.screen.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tickets.store_cost.presentation.theme.AppStyles

/**
 * Mensaje de error homogéneo para todas las pantallas.
 */
@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = message,
        color = AppStyles.Colors.error,
        style = AppStyles.Typography.body,
        modifier = modifier
    )
}

/**
 * Mensaje de éxito homogéneo para todas las pantallas.
 */
@Composable
fun SuccessMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = message,
        color = AppStyles.Colors.success,
        style = AppStyles.Typography.body,
        modifier = modifier
    )
}

/**
 * Indicador de carga lineal reutilizable.
 */
@Composable
fun LoadingBar(modifier: Modifier = Modifier) {
    LinearProgressIndicator(modifier = modifier.fillMaxWidth())
}
