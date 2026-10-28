package com.tickets.store_cost.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickets.store_cost.domain.model.DailyClosing
import com.tickets.store_cost.domain.model.ErrorCode
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
import kotlinx.coroutines.flow.flowOf
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
    private val todayRange = MutableStateFlow(currentDayRange())
    private val closingState = MutableStateFlow<Result<DailyClosing>?>(null)
    private val pendingClosing = MutableStateFlow<DailyClosing?>(null)

    /**
     * Resumen del día actual, usado para habilitar el botón de cierre.
     *
     * Solo se consulta cuando la pestaña activa es Diario: en el resto de las
     * pestañas el botón de cierre no está visible y la consulta sería trabajo
     * de base de datos desperdiciado.
     */
    private val todaySummary: StateFlow<Result<SalesSummary>> =
        combine(todayRange, query.map { it.period }) { range, period -> range to period }
            .flatMapLatest { (range, period) ->
                if (period == ClosingPeriod.DAILY) {
                    getSalesSummaryUseCase.observeSalesSummary(range.first, range.second)
                } else {
                    flowOf(Result.Success(SalesSummary(0.0, 0, emptyMap())))
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = Result.Loading
            )

    /**
     * El DAO expone un `Flow`, por lo que cada cambio en la tabla re-emite sin
     * necesidad de refrescar manualmente.
     */
    private val summaryState: Flow<Result<SalesSummary>> =
        query.flatMapLatest { currentQuery ->
            getSalesSummaryUseCase.observeSalesSummary(
                currentQuery.startTime,
                currentQuery.endTime
            )
        }

    /**
     * El formulario se combina por separado del resumen: escribir en un campo
     * no debe reiniciar la consulta a Room ni retrasar la actualización del
     * estado (lo que provocaba saltos del cursor en los `TextField`).
     */
    private val baseState: Flow<SalesUiState> =
        combine(formState, query, summaryState) { form, currentQuery, summary ->
            SalesUiState(
                period = currentQuery.period,
                startTime = currentQuery.startTime,
                endTime = currentQuery.endTime,
                amount = form.amount,
                quantity = form.quantity,
                registerState = form.registerState,
                summary = summary
            )
        }

    val salesUiState: StateFlow<SalesUiState> =
        combine(
            baseState,
            todaySummary,
            closingState,
            pendingClosing,
            todayRange
        ) { base, today, closing, pending, dayRange ->
            base.copy(
                todayStart = dayRange.first,
                todayEnd = dayRange.second,
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
                    registerState = Result.Error(ErrorCode.INVALID_SALE_AMOUNT_AND_QUANTITY)
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

    /**
     * Recalcula el rango del día en curso y recarga el cierre guardado.
     *
     * Se invoca al entrar a la pantalla para evitar que, pasada la medianoche,
     * la pestaña Diario siga mostrando el día anterior.
     */
    fun refreshToday() {
        val previous = todayRange.value
        val current = currentDayRange()
        if (previous != current) {
            todayRange.value = current
            val activeQuery = query.value
            val wasShowingToday = activeQuery.startTime == previous.first &&
                activeQuery.endTime == previous.second
            if (activeQuery.period == ClosingPeriod.DAILY && wasShowingToday) {
                query.value = activeQuery.copy(
                    startTime = current.first,
                    endTime = current.second
                )
            }
        }
        loadTodayClosing()
    }

    /**
     * Calcula el total del día y prepara el diálogo de confirmación.
     */
    fun onRequestCloseDay() {
        if (!salesUiState.value.canCloseDay) {
            closingState.value = Result.Error(ErrorCode.NO_SALES_TODAY)
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
        }
    }

    fun onDismissClosingDialog() {
        pendingClosing.value = null
    }

    private fun loadTodayClosing() {
        viewModelScope.launch {
            when (val result = getDailyClosingUseCase()) {
                // Un día sin cierre no debe conservar el cierre del día anterior.
                is Result.Success -> closingState.value =
                    result.data?.let { Result.Success(it) }
                is Result.Error -> closingState.value = result
                Result.Loading -> Unit
            }
        }
    }

    private fun initialQuery(): SalesQuery {
        val (start, end) = rangeFor(ClosingPeriod.DAILY)
        return SalesQuery(period = ClosingPeriod.DAILY, startTime = start, endTime = end)
    }

    private fun currentDayRange(): Pair<Long, Long> = todayIn().toDayRange()

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
