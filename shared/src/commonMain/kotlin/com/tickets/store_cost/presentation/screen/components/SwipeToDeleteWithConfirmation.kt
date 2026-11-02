package com.tickets.store_cost.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import com.tickets.store_cost.presentation.theme.AppStyles
import com.tickets.store_cost.resources.Res
import com.tickets.store_cost.resources.common_cancel
import com.tickets.store_cost.resources.common_delete
import com.tickets.store_cost.resources.common_delete_action
import com.tickets.store_cost.resources.common_delete_confirmation_message
import com.tickets.store_cost.resources.common_delete_confirmation_title
import org.jetbrains.compose.resources.stringResource

/**
 * Envoltorio genérico de *swipe-to-dismiss* con confirmación.
 *
 * El gesto nunca elimina directamente: al superar el umbral la fila vuelve a su
 * posición y se solicita abrir el diálogo modal. La visibilidad del diálogo la
 * controla el ViewModel mediante [confirmVisible], de modo que la selección del
 * elemento a borrar queda en el estado inmutable de la pantalla.
 *
 * Se apoya en [SwipeToDismissBox] de Material 3 para el arrastre y el retorno
 * animado, y se reutiliza tanto en el historial de ventas como en la lista de
 * precios calculados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteWithConfirmation(
    confirmVisible: Boolean,
    onDeleteRequested: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissConfirmation: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val currentOnDeleteRequested by rememberUpdatedState(onDeleteRequested)
    val currentEnabled by rememberUpdatedState(enabled)

    val density = LocalDensity.current
    val thresholdPx = with(density) { AppStyles.Sizes.swipeDismissThreshold.toPx() }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart && currentEnabled) {
                currentOnDeleteRequested()
            }
            // La fila nunca se elimina con el gesto: el diálogo decide.
            false
        },
        positionalThreshold = { thresholdPx }
    )
    val shape = RoundedCornerShape(AppStyles.Sizes.swipeCornerRadius)

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape),
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = enabled,
        backgroundContent = {
            // Fondo de la acción destructiva: solo se ve mientras la fila se arrastra.
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.error),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.common_delete),
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.padding(end = AppStyles.Spacing.lg)
                    )
                }
            }
        },
        content = { content() }
    )

    if (confirmVisible) {
        AlertDialog(
            onDismissRequest = onDismissConfirmation,
            modifier = Modifier.width(AppStyles.Sizes.confirmationDialogWidth),
            title = {
                Text(text = stringResource(Res.string.common_delete_confirmation_title))
            },
            text = {
                Text(text = stringResource(Res.string.common_delete_confirmation_message))
            },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text(text = stringResource(Res.string.common_delete_action))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissConfirmation) {
                    Text(text = stringResource(Res.string.common_cancel))
                }
            }
        )
    }
}
