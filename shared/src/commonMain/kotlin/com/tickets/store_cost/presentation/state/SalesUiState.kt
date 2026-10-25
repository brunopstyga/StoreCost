package com.tickets.store_cost.presentation.state

import com.tickets.store_cost.domain.model.DailyClosing
import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import com.tickets.store_cost.domain.model.SalesSummary
import com.tickets.store_cost.domain.model.getOrNull

/**
 * Períodos soportados para el cierre de jornada y reportes.
 *
 * [HISTORY] no representa un rango de fechas: expone el historial completo
 * de transacciones (ver [SalesUiState.isHistory]).
 */
enum class ClosingPeriod {
    DAILY,
    HISTORY,
    MONTHLY
}

/**
 * Estado inmutable de la pantalla "Ventas y Cierre".
 * Encapsula el formulario de registro directo, el período consultado,
 * el historial de transacciones y el cierre diario.
 */
data class SalesUiState(
    val period: ClosingPeriod = ClosingPeriod.DAILY,
    val startTime: Long = 0L,
    val endTime: Long = Long.MAX_VALUE,
    val amount: String = "1000.0",
    val quantity: String = "1",
    val registerState: Result<Unit>? = null,
    val summary: Result<SalesSummary> = Result.Loading,
    val todayTotal: Double = 0.0,
    val todaySalesCount: Int = 0,
    val pendingClosing: DailyClosing? = null,
    val closingState: Result<DailyClosing>? = null
) {
    val totalBilled: Double
        get() = summary.getOrNull()?.totalBilled ?: 0.0

    val totalSalesCount: Int
        get() = summary.getOrNull()?.totalSalesCount ?: 0

    val breakdown: Map<OperationType, Double>
        get() = summary.getOrNull()?.breakdown ?: emptyMap()

    val sales: List<Sale>
        get() = summary.getOrNull()?.sales ?: emptyList()

    /**
     * `true` cuando la pestaña activa es el historial completo de transacciones.
     */
    val isHistory: Boolean
        get() = period == ClosingPeriod.HISTORY

    val isDaily: Boolean
        get() = period == ClosingPeriod.DAILY

    val isMonthly: Boolean
        get() = period == ClosingPeriod.MONTHLY

    /**
     * El formulario de registro y el cierre diario solo se muestran en la pestaña Diario.
     */
    val showSaleForm: Boolean
        get() = period == ClosingPeriod.DAILY

    val isLoading: Boolean
        get() = summary is Result.Loading

    val isRegistering: Boolean
        get() = registerState is Result.Loading

    val isRegistered: Boolean
        get() = registerState is Result.Success

    val registerError: Result.Error?
        get() = registerState as? Result.Error

    val historyError: Result.Error?
        get() = summary as? Result.Error

    val registerErrorMessage: String?
        get() = registerError?.message

    val historyErrorMessage: String?
        get() = historyError?.message

    val errorMessage: String?
        get() = registerErrorMessage ?: historyErrorMessage

    val canCloseDay: Boolean
        get() = todaySalesCount > 0

    val isClosing: Boolean
        get() = closingState is Result.Loading

    val savedClosing: DailyClosing?
        get() = (closingState as? Result.Success<DailyClosing>)?.data

    val closingError: Result.Error?
        get() = closingState as? Result.Error

    val closingErrorMessage: String?
        get() = closingError?.message
}
