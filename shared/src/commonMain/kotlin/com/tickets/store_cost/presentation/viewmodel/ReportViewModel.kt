package com.tickets.store_cost.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickets.store_cost.core.logging.AppLogger
import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.MonthlyReport
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.repository.IReportFileWriter
import com.tickets.store_cost.domain.usecase.GenerateMonthlyReportUseCase
import com.tickets.store_cost.presentation.state.ReportUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

/**
 * ViewModel del reporte mensual: genera el CSV, lo persiste mediante
 * [IReportFileWriter] y expone el resultado como un [StateFlow] cálido.
 */
@KoinViewModel
class ReportViewModel(
    private val generateMonthlyReportUseCase: GenerateMonthlyReportUseCase,
    private val reportFileWriter: IReportFileWriter
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun onExportMonthlyReport(year: Int, month: Int) {
        if (_uiState.value is ReportUiState.Loading) return

        _uiState.value = ReportUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val reportResult = generateMonthlyReportUseCase(year, month)) {
                is Result.Success -> writeReport(reportResult.data)
                is Result.Error -> {
                    AppLogger.e(
                        "No se pudo generar el reporte $year-$month (${reportResult.code.name}): " +
                            reportResult.message
                    )
                    ReportUiState.Error(reportResult.code, reportResult.detail)
                }
                Result.Loading -> ReportUiState.Error(ErrorCode.GENERATE_REPORT_FAILED)
            }
        }
    }

    /**
     * Marca el resultado como consumido para evitar relanzar el correo tras
     * una recomposición o recreación de la pantalla.
     */
    fun onReportHandled() {
        if (_uiState.value is ReportUiState.ExportSuccess) {
            _uiState.value = ReportUiState.Idle
        }
    }

    private suspend fun writeReport(report: MonthlyReport): ReportUiState =
        when (val writeResult = reportFileWriter.write(report)) {
            is Result.Success -> ReportUiState.ExportSuccess(
                filePath = writeResult.data
            )

            is Result.Error -> {
                AppLogger.e(
                    "No se pudo guardar el reporte (${writeResult.code.name}): ${writeResult.message}"
                )
                ReportUiState.Error(writeResult.code, writeResult.detail)
            }
            Result.Loading -> ReportUiState.Error(ErrorCode.SAVE_REPORT_FAILED)
        }
}
