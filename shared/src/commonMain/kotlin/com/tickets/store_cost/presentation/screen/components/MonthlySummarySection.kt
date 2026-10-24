package com.tickets.store_cost.presentation.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.SalesSummary
import com.tickets.store_cost.domain.model.getOrNull
import com.tickets.store_cost.presentation.state.ReportUiState
import com.tickets.store_cost.presentation.util.displayName
import com.tickets.store_cost.presentation.util.formatCurrency
import com.tickets.store_cost.presentation.util.formatMonthYear
import com.tickets.store_cost.resources.Res
import com.tickets.store_cost.resources.common_operations
import com.tickets.store_cost.resources.monthly_average_ticket
import com.tickets.store_cost.resources.monthly_by_modality
import com.tickets.store_cost.resources.monthly_empty
import com.tickets.store_cost.resources.monthly_export_button
import com.tickets.store_cost.resources.monthly_exporting
import com.tickets.store_cost.resources.monthly_summary_title
import com.tickets.store_cost.resources.monthly_total_sold
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

/**
 * Resumen de las ventas del mes en curso: total vendido, cantidad de
 * operaciones, ticket promedio y desglose por modalidad. También expone la
 * exportación manual del reporte mensual en CSV.
 */
@Composable
fun MonthlySummarySection(
    startTime: Long,
    summary: Result<SalesSummary>,
    reportState: ReportUiState,
    onExportMonthlyReport: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val salesSummary = summary.getOrNull()
    val totalBilled = salesSummary?.totalBilled ?: 0.0
    val salesCount = salesSummary?.totalSalesCount ?: 0
    val breakdown = salesSummary?.breakdown ?: emptyMap()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(Res.string.monthly_summary_title, formatMonthYear(startTime)),
            style = MaterialTheme.typography.titleMedium
        )

        if (summary is Result.Loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        (summary as? Result.Error)?.let { error ->
            Text(
                text = error.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (summary is Result.Success) {
            if (salesCount == 0) {
                Text(
                    text = stringResource(Res.string.monthly_empty),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.monthly_total_sold),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatCurrency(totalBilled),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SummaryValue(
                                label = stringResource(Res.string.common_operations),
                                value = salesCount.toString()
                            )
                            SummaryValue(
                                label = stringResource(Res.string.monthly_average_ticket),
                                value = formatCurrency(totalBilled / salesCount)
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                val month = Instant.fromEpochMilliseconds(startTime)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                onExportMonthlyReport(month.year, month.monthNumber)
            },
            enabled = reportState !is ReportUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(
                    if (reportState is ReportUiState.Loading) {
                        Res.string.monthly_exporting
                    } else {
                        Res.string.monthly_export_button
                    }
                )
            )
        }

        if (reportState is ReportUiState.Loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        (reportState as? ReportUiState.Error)?.let { error ->
            Text(
                text = error.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SummaryValue(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
