package com.tickets.store_cost.presentation.state

/**
 * Estado del flujo de exportación del reporte mensual.
 */
sealed interface ReportUiState {
    data object Idle : ReportUiState

    data object Loading : ReportUiState

    data class ExportSuccess(
        val filePath: String
    ) : ReportUiState

    data class Error(val message: String) : ReportUiState
}
