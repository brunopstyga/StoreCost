package com.tickets.store_cost.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.SalesSummary
import com.tickets.store_cost.domain.util.toDayRange
import com.tickets.store_cost.presentation.screen.components.AppCard
import com.tickets.store_cost.presentation.screen.components.AppOutlinedTextField
import com.tickets.store_cost.presentation.screen.components.ErrorMessage
import com.tickets.store_cost.presentation.screen.components.LoadingBar
import com.tickets.store_cost.presentation.screen.components.MonthlySummarySection
import com.tickets.store_cost.presentation.screen.components.SectionTitle
import com.tickets.store_cost.presentation.screen.components.SuccessMessage
import com.tickets.store_cost.presentation.screen.components.TransactionHistorySection
import com.tickets.store_cost.presentation.state.ClosingPeriod
import com.tickets.store_cost.presentation.state.ReportUiState
import com.tickets.store_cost.presentation.state.SalesUiState
import com.tickets.store_cost.presentation.theme.AppStyles
import com.tickets.store_cost.presentation.util.displayMessage
import com.tickets.store_cost.presentation.util.formatCurrency
import com.tickets.store_cost.presentation.util.formatDateRange
import com.tickets.store_cost.presentation.util.formatTimestamp
import com.tickets.store_cost.presentation.viewmodel.ReportViewModel
import com.tickets.store_cost.presentation.viewmodel.SalesViewModel
import com.tickets.store_cost.resources.Res
import com.tickets.store_cost.resources.common_accept
import com.tickets.store_cost.resources.common_cancel
import com.tickets.store_cost.resources.common_confirm
import com.tickets.store_cost.resources.common_operations_count
import com.tickets.store_cost.resources.common_quantity_label
import com.tickets.store_cost.resources.period_daily
import com.tickets.store_cost.resources.period_history
import com.tickets.store_cost.resources.period_monthly
import com.tickets.store_cost.resources.sales_amount_label
import com.tickets.store_cost.resources.sales_change_date
import com.tickets.store_cost.resources.sales_closing_button
import com.tickets.store_cost.resources.sales_closing_saved
import com.tickets.store_cost.resources.sales_closing_saving
import com.tickets.store_cost.resources.sales_closing_total
import com.tickets.store_cost.resources.sales_closing_total_today
import com.tickets.store_cost.resources.sales_confirm_closing_title
import com.tickets.store_cost.resources.sales_period
import com.tickets.store_cost.resources.sales_register_button
import com.tickets.store_cost.resources.sales_register_title
import com.tickets.store_cost.resources.sales_registered_success
import com.tickets.store_cost.resources.sales_registering
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pantalla "Ventas y Cierre". Recolecta el [SalesUiState] del ViewModel y
 * renderiza una vista por pestaña:
 * - Diario: registro directo de ventas y cierre del día.
 * - Historial: listado completo de transacciones.
 * - Mensual: resumen de ventas del mes y exportación del reporte.
 */
@Composable
fun VentasScreen(
    onSendReport: (String) -> Unit = {},
    viewModel: SalesViewModel = koinViewModel(),
    reportViewModel: ReportViewModel = koinViewModel()
) {
    val state by viewModel.salesUiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshToday()
    }

    VentasContent(
        state = state,
        onAmountChange = viewModel::onAmountChange,
        onQuantityChange = viewModel::onQuantityChange,
        onRegisterSale = viewModel::onRegisterSale,
        onPeriodSelected = viewModel::onPeriodSelected,
        onDateRangeSelected = viewModel::onDateRangeSelected,
        onRequestCloseDay = viewModel::onRequestCloseDay,
        onConfirmCloseDay = viewModel::onConfirmCloseDay,
        onDismissClosingDialog = viewModel::onDismissClosingDialog,
        monthlySummary = { modifier ->
            MonthlySummaryRoute(
                startTime = state.startTime,
                summary = state.summary,
                reportViewModel = reportViewModel,
                onSendReport = onSendReport,
                modifier = modifier
            )
        }
    )
}

/**
 * Aísla la recolección de [ReportViewModel] a la pestaña Mensual: un cambio en
 * el estado de exportación no debe recomponer Diario ni Historial.
 */
@Composable
private fun MonthlySummaryRoute(
    startTime: Long,
    summary: Result<SalesSummary>,
    reportViewModel: ReportViewModel,
    onSendReport: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val reportState by reportViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(reportState) {
        val success = reportState as? ReportUiState.ExportSuccess ?: return@LaunchedEffect
        onSendReport(success.filePath)
        reportViewModel.onReportHandled()
    }

    MonthlySummarySection(
        startTime = startTime,
        summary = summary,
        reportState = reportState,
        onExportMonthlyReport = reportViewModel::onExportMonthlyReport,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VentasContent(
    state: SalesUiState,
    onAmountChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onRegisterSale: () -> Unit,
    onPeriodSelected: (ClosingPeriod) -> Unit,
    onDateRangeSelected: (Long, Long) -> Unit,
    onRequestCloseDay: () -> Unit,
    onConfirmCloseDay: () -> Unit,
    onDismissClosingDialog: () -> Unit,
    monthlySummary: @Composable (Modifier) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppStyles.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppStyles.Spacing.md)
    ) {
        TabRow(selectedTabIndex = state.period.ordinal) {
            ClosingPeriod.entries.forEach { period ->
                Tab(
                    selected = state.period == period,
                    onClick = { onPeriodSelected(period) },
                    text = { Text(text = stringResource(periodLabel(period))) }
                )
            }
        }

        when (state.period) {
            ClosingPeriod.DAILY -> DailySalesContent(
                state = state,
                onAmountChange = onAmountChange,
                onQuantityChange = onQuantityChange,
                onRegisterSale = onRegisterSale,
                onChangeDate = { showDatePicker = true },
                onRequestCloseDay = onRequestCloseDay
            )

            ClosingPeriod.HISTORY -> TransactionHistorySection(
                sales = state.sales,
                todayStart = state.todayStart,
                todayEnd = state.todayEnd,
                isLoading = state.isLoading,
                error = state.historyError,
                modifier = Modifier.weight(1f)
            )

            ClosingPeriod.MONTHLY -> monthlySummary(Modifier.weight(1f))
        }
    }

    state.pendingClosing?.let { closing ->
        AlertDialog(
            onDismissRequest = onDismissClosingDialog,
            title = { Text(text = stringResource(Res.string.sales_confirm_closing_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppStyles.Spacing.xs)) {
                    Text(
                        text = stringResource(
                            Res.string.sales_closing_total_today,
                            formatCurrency(closing.totalAmount)
                        )
                    )
                    Text(
                        text = stringResource(
                            Res.string.common_operations_count,
                            closing.salesCount
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirmCloseDay) {
                    Text(text = stringResource(Res.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissClosingDialog) {
                    Text(text = stringResource(Res.string.common_cancel))
                }
            }
        )
    }

    if (showDatePicker && state.isDaily) {
        SalesDatePicker(
            onDismiss = { showDatePicker = false },
            onDateSelected = { startTime, endTime ->
                onDateRangeSelected(startTime, endTime)
                showDatePicker = false
            }
        )
    }
}

/**
 * Vista de la pestaña Diario: período consultado, formulario de registro
 * directo y cierre del día.
 */
@Composable
private fun DailySalesContent(
    state: SalesUiState,
    onAmountChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onRegisterSale: () -> Unit,
    onChangeDate: () -> Unit,
    onRequestCloseDay: () -> Unit
) {
    val period = remember(state.startTime, state.endTime) {
        formatDateRange(state.startTime, state.endTime)
    }
    val decimalKeyboard = remember { KeyboardOptions(keyboardType = KeyboardType.Decimal) }
    val numberKeyboard = remember { KeyboardOptions(keyboardType = KeyboardType.Number) }

    if (state.isLoading) {
        LoadingBar()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.sales_period, period),
            style = AppStyles.Typography.caption,
            color = AppStyles.Colors.secondaryText
        )
        TextButton(onClick = onChangeDate) {
            Text(text = stringResource(Res.string.sales_change_date))
        }
    }

    state.historyError?.let { error ->
        ErrorMessage(error.displayMessage())
    }

    SectionTitle(text = stringResource(Res.string.sales_register_title))

    AppOutlinedTextField(
        value = state.amount,
        onValueChange = onAmountChange,
        label = stringResource(Res.string.sales_amount_label),
        keyboardOptions = decimalKeyboard
    )

    AppOutlinedTextField(
        value = state.quantity,
        onValueChange = onQuantityChange,
        label = stringResource(Res.string.common_quantity_label),
        keyboardOptions = numberKeyboard
    )

    Button(
        onClick = onRegisterSale,
        enabled = !state.isRegistering,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(if (state.isRegistering) Res.string.sales_registering else Res.string.sales_register_button))
    }

    if (state.isRegistered) {
        SuccessMessage(stringResource(Res.string.sales_registered_success))
    }

    state.registerError?.let { error ->
        ErrorMessage(error.displayMessage())
    }

    Button(
        onClick = onRequestCloseDay,
        enabled = state.canCloseDay && !state.isClosing,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(if (state.isClosing) Res.string.sales_closing_saving else Res.string.sales_closing_button))
    }

    state.savedClosing?.let { closing ->
        AppCard(verticalSpacing = AppStyles.Spacing.hairline) {
            Text(
                text = stringResource(Res.string.sales_closing_saved),
                style = AppStyles.Typography.itemTitle
            )
            Text(
                text = stringResource(
                    Res.string.sales_closing_total,
                    formatCurrency(closing.totalAmount)
                ),
                style = AppStyles.Typography.sectionTitle
            )
            Text(
                text = stringResource(
                    Res.string.common_operations_count,
                    closing.salesCount
                ),
                style = AppStyles.Typography.caption
            )
            Text(
                text = formatTimestamp(closing.closedAt),
                style = AppStyles.Typography.caption,
                color = AppStyles.Colors.secondaryText
            )
        }
    }

    state.closingError?.let { error ->
        ErrorMessage(error.displayMessage())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalesDatePicker(
    onDismiss: () -> Unit,
    onDateSelected: (Long, Long) -> Unit
) {
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val todayUtcMillis = remember {
        Clock.System.now()
            .toLocalDateTime(timeZone)
            .date
            .atStartOfDayIn(TimeZone.UTC)
            .toEpochMilliseconds()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = todayUtcMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = datePickerState.selectedDateMillis != null,
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC)
                            .date
                        val (start, end) = date.toDayRange(timeZone)
                        onDateSelected(start, end)
                    }
                }
            ) {
                Text(text = stringResource(Res.string.common_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.common_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun periodLabel(period: ClosingPeriod): StringResource = when (period) {
    ClosingPeriod.DAILY -> Res.string.period_daily
    ClosingPeriod.HISTORY -> Res.string.period_history
    ClosingPeriod.MONTHLY -> Res.string.period_monthly
}
