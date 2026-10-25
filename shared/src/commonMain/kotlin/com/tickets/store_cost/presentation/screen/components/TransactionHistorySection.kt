package com.tickets.store_cost.presentation.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tickets.store_cost.domain.model.Sale
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
 */
@Composable
fun TransactionHistorySection(
    sales: List<Sale>,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(Res.string.history_title),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = stringResource(Res.string.common_operations_count, sales.size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (isLoading) {
            LoadingBar()
        }

        errorMessage?.let { message ->
            ErrorMessage(message)
        }

        if (sales.isEmpty()) {
            if (!isLoading) {
                Text(
                    text = stringResource(Res.string.history_empty),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = sales, key = { it.id }) { sale ->
                    SaleHistoryItem(sale = sale)
                }
            }
        }
    }
}

@Composable
private fun SaleHistoryItem(sale: Sale) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatCurrency(sale.subtotal),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = formatTimestamp(sale.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(
                    Res.string.history_item_detail,
                    sale.quantity,
                    formatCurrency(sale.unitPrice)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
