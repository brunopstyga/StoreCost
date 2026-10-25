package com.tickets.store_cost.presentation.state

import com.tickets.store_cost.domain.model.ErrorCode

/**
 * Estado del flujo de exportación del reporte mensual.
 */
sealed interface ReportUiState {
    data object Idle : ReportUiState

    data object Loading : ReportUiState

    data class ExportSuccess(
        val filePath: String
    ) : ReportUiState

    /**
     * [detail] conserva el mensaje técnico para logs; la UI muestra el texto
     * localizado de [code].
     */
    data class Error(
        val code: ErrorCode,
        val detail: String? = null
    ) : ReportUiState
}
