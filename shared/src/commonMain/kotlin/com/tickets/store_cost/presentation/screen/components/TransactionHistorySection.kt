package com.tickets.store_cost.presentation.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import com.tickets.store_cost.presentation.theme.AppStyles
import com.tickets.store_cost.presentation.util.displayMessage
import com.tickets.store_cost.presentation.util.formatCurrency
import com.tickets.store_cost.presentation.util.formatTimestamp
import com.tickets.store_cost.resources.Res
import com.tickets.store_cost.resources.common_operations_count
import com.tickets.store_cost.resources.history_empty
import com.tickets.store_cost.resources.history_item_detail
import com.tickets.store_cost.resources.history_title
import org.jetbrains.compose.resources.stringResource

/**
 * Sección con el historial completo de transacciones.
 * Se muestra en la pestaña "Historial" de la pantalla "Ventas y Cierre".
 *
 * [todayStart]/[todayEnd] llegan desde el `SalesUiState` para no leer el reloj
 * ni la zona horaria durante la composición.
 */
@Composable
fun TransactionHistorySection(
    sales: List<Sale>,
    todayStart: Long,
    todayEnd: Long,
    isLoading: Boolean,
    error: Result.Error?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppStyles.Spacing.sm)
    ) {
        SectionTitle(text = stringResource(Res.string.history_title))

        Text(
            text = stringResource(Res.string.common_operations_count, sales.size),
            style = AppStyles.Typography.caption,
            color = AppStyles.Colors.secondaryText
        )

        if (isLoading) {
            LoadingBar()
        }

        error?.let { error ->
            ErrorMessage(error.displayMessage())
        }

        if (sales.isEmpty()) {
            if (!isLoading) {
                Text(
                    text = stringResource(Res.string.history_empty),
                    style = AppStyles.Typography.body
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppStyles.Spacing.sm)
            ) {
                items(items = sales, key = { it.id }) { sale ->
                    SaleHistoryItem(
                        sale = sale,
                        isToday = sale.timestamp in todayStart..todayEnd
                    )
                }
            }
        }
    }
}

@Composable
private fun SaleHistoryItem(sale: Sale, isToday: Boolean) {
    val subtotal = remember(sale.id, sale.subtotal) { formatCurrency(sale.subtotal) }
    val timestamp = remember(sale.id, sale.timestamp) { formatTimestamp(sale.timestamp) }
    val unitPrice = remember(sale.id, sale.unitPrice) { formatCurrency(sale.unitPrice) }

    AppCard(
        verticalSpacing = AppStyles.Spacing.xs,
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) {
                AppStyles.Colors.todayHistoryCard
            } else {
                AppStyles.Colors.pastHistoryCard
            }
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subtotal,
                style = AppStyles.Typography.itemAmount
            )
            Text(
                text = timestamp,
                style = AppStyles.Typography.caption,
                color = AppStyles.Colors.secondaryText
            )
        }
        Text(
            text = stringResource(
                Res.string.history_item_detail,
                sale.quantity,
                unitPrice
            ),
            style = AppStyles.Typography.body
        )
    }
}
