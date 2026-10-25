package com.tickets.store_cost.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickets.store_cost.domain.model.DailyClosing
import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import com.tickets.store_cost.domain.model.SalesSummary
import com.tickets.store_cost.domain.model.getOrNull
import com.tickets.store_cost.domain.usecase.CloseDayUseCase
import com.tickets.store_cost.domain.usecase.GetDailyClosingUseCase
import com.tickets.store_cost.domain.usecase.GetSalesSummaryUseCase
import com.tickets.store_cost.domain.usecase.RegisterSaleUseCase
import com.tickets.store_cost.domain.util.roundToCents
import com.tickets.store_cost.domain.util.todayIn
import com.tickets.store_cost.domain.util.toDayRange
import com.tickets.store_cost.domain.util.toMonthRange
import com.tickets.store_cost.presentation.state.ClosingPeriod
import com.tickets.store_cost.presentation.state.SalesUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.android.annotation.KoinViewModel

/**
 * ViewModel UDF de la pantalla "Ventas y Cierre".
 * Combina el formulario de registro directo, el rango del período, el resumen
 * del día y el estado del cierre diario en un [StateFlow] cálido.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class SalesViewModel(
    private val registerSaleUseCase: RegisterSaleUseCase,
    private val getSalesSummaryUseCase: GetSalesSummaryUseCase,
    private val closeDayUseCase: CloseDayUseCase,
    private val getDailyClosingUseCase: GetDailyClosingUseCase
) : ViewModel() {

    private data class FormState(
        val amount: String = "1000.0",
        val quantity: String = "1",
        val registerState: Result<Unit>? = null
    )

    private data class SalesQuery(
        val period: ClosingPeriod,
        val startTime: Long,
        val endTime: Long
    )

    private val formState = MutableStateFlow(FormState())
    private val query = MutableStateFlow(initialQuery())
    private val refreshTick = MutableStateFlow(0)
    private val closingState = MutableStateFlow<Result<DailyClosing>?>(null)
    private val pendingClosing = MutableStateFlow<DailyClosing?>(null)

    private val todayRange: Pair<Long, Long> = rangeFor(ClosingPeriod.DAILY)

    /**
     * Resumen del día actual, usado para habilitar el botón de cierre
     * independientemente de la pestaña seleccionada.
     */
    private val todaySummary: StateFlow<Result<SalesSummary>> =
        refreshTick
            .flatMapLatest {
                getSalesSummaryUseCase.observeSalesSummary(todayRange.first, todayRange.second)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = Result.Loading
            )

    private val baseState: Flow<SalesUiState> =
        combine(formState, query, refreshTick) { form, currentQuery, _ ->
            Triple(form, currentQuery, Unit)
        }
            .flatMapLatest { (form, currentQuery, _) ->
                getSalesSummaryUseCase
                    .observeSalesSummary(currentQuery.startTime, currentQuery.endTime)
                    .map { result ->
                        SalesUiState(
                            period = currentQuery.period,
                            startTime = currentQuery.startTime,
                            endTime = currentQuery.endTime,
                            amount = form.amount,
                            quantity = form.quantity,
                            registerState = form.registerState,
                            summary = result
                        )
                    }
            }

    val salesUiState: StateFlow<SalesUiState> =
        combine(
            baseState,
            todaySummary,
            closingState,
            pendingClosing
        ) { base, today, closing, pending ->
            base.copy(
                todayTotal = today.getOrNull()?.totalBilled ?: 0.0,
                todaySalesCount = today.getOrNull()?.totalSalesCount ?: 0,
                pendingClosing = pending,
                closingState = closing
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SalesUiState()
        )

    init {
        loadTodayClosing()
    }

    fun onAmountChange(value: String) {
        formState.update { it.copy(amount = value, registerState = null) }
    }

    fun onQuantityChange(value: String) {
        formState.update { it.copy(quantity = value, registerState = null) }
    }

    /**
     * Registra una venta directa con el importe y la cantidad ingresados.
     * El importe ya incluye la modalidad, por lo que se persiste tal cual
     * (sin volver a aplicar recargos).
     */
    fun onRegisterSale() {
        val current = formState.value
        val amount = current.amount.toDoubleOrNull()
        val quantity = current.quantity.toIntOrNull()

        if (amount == null || amount <= 0.0 || quantity == null || quantity <= 0) {
            formState.update {
                it.copy(
                    registerState = Result.Error("El importe y la cantidad deben ser mayores a cero.")
                )
            }
            return
        }

        viewModelScope.launch {
            formState.update { it.copy(registerState = Result.Loading) }

            val unitPrice = amount.roundToCents()
            val subtotal = (unitPrice * quantity).roundToCents()

            val result = registerSaleUseCase(
                Sale(
                    basePrice = unitPrice,
                    quantity = quantity,
                    operationType = DEFAULT_OPERATION_TYPE,
                    unitPrice = unitPrice,
                    subtotal = subtotal,
                    timestamp = Clock.System.now().toEpochMilliseconds()
                )
            )
            formState.update { it.copy(registerState = result) }
            if (result is Result.Success) {
                refreshSales()
            }
        }
    }

    fun onPeriodSelected(period: ClosingPeriod) {
        val (start, end) = rangeFor(period)
        query.value = SalesQuery(period = period, startTime = start, endTime = end)
    }

    fun onDateRangeSelected(startTime: Long, endTime: Long) {
        // El selector de fecha solo aplica al período diario: Historial usa el rango
        // completo y Mensual siempre abarca el mes en curso.
        if (query.value.period != ClosingPeriod.DAILY) return
        query.value = query.value.copy(startTime = startTime, endTime = endTime)
    }

    fun refreshSales() {
        refreshTick.value += 1
    }

    /**
     * Calcula el total del día y prepara el diálogo de confirmación.
     */
    fun onRequestCloseDay() {
        if (!salesUiState.value.canCloseDay) {
            closingState.value = Result.Error("No hay ventas registradas para el día de hoy.")
            return
        }

        viewModelScope.launch {
            when (val result = closeDayUseCase.calculateDailyClosing()) {
                is Result.Success -> pendingClosing.value = result.data
                is Result.Error -> closingState.value = result
                Result.Loading -> Unit
            }
        }
    }

    fun onConfirmCloseDay() {
        val closing = pendingClosing.value ?: return

        viewModelScope.launch {
            pendingClosing.value = null
            closingState.value = Result.Loading

            val result = closeDayUseCase(closing)
            closingState.value = result
            if (result is Result.Success) {
                refreshSales()
            }
        }
    }

    fun onDismissClosingDialog() {
        pendingClosing.value = null
    }

    private fun loadTodayClosing() {
        viewModelScope.launch {
            when (val result = getDailyClosingUseCase()) {
                is Result.Success -> result.data?.let { closingState.value = Result.Success(it) }
                is Result.Error -> closingState.value = result
                Result.Loading -> Unit
            }
        }
    }

    private fun initialQuery(): SalesQuery {
        val (start, end) = rangeFor(ClosingPeriod.DAILY)
        return SalesQuery(period = ClosingPeriod.DAILY, startTime = start, endTime = end)
    }

    /**
     * Calcula el rango [start, end] en milisegundos para el período seleccionado
     * usando kotlinx-datetime. El historial usa el rango completo para incluir
     * todas las transacciones sin importar la fecha.
     */
    private fun rangeFor(period: ClosingPeriod): Pair<Long, Long> = when (period) {
        ClosingPeriod.HISTORY -> Long.MIN_VALUE to Long.MAX_VALUE
        ClosingPeriod.DAILY -> todayIn().toDayRange()
        ClosingPeriod.MONTHLY -> todayIn().toMonthRange()
    }

    private companion object {
        /**
         * La modalidad no se elige en el formulario y el importe ya la incluye;
         * se persiste Factura A como valor interno que la UI no muestra.
         */
        val DEFAULT_OPERATION_TYPE: OperationType = OperationType.FACTURA_A
    }
}
