package com.tickets.store_cost.presentation.state

import androidx.compose.runtime.Immutable
import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.PriceConsultation
import com.tickets.store_cost.domain.model.Result

/**
 * Estado inmutable de la pantalla de Gestión de Precios.
 * Encapsula los valores del formulario, el historial de consultas calculadas
 * y los estados [Result] de la carga del historial y del guardado.
 */
@Immutable
data class ConsultingUiState(
    val basePrice: String = "1000.0",
    val quantity: String = "1",
    val selectedModality: OperationType = OperationType.FACTURA_A,
    val consultations: Result<List<PriceConsultation>> = Result.Loading,
    val saveState: Result<Unit>? = null,
    val pendingDeletionId: Long? = null,
    val deleteState: Result<Unit>? = null
) {
    val isSaving: Boolean
        get() = saveState is Result.Loading

    val isSaved: Boolean
        get() = saveState is Result.Success

    val saveError: Result.Error?
        get() = saveState as? Result.Error

    val historyError: Result.Error?
        get() = consultations as? Result.Error

    val saveErrorMessage: String?
        get() = saveError?.message

    val historyErrorMessage: String?
        get() = historyError?.message

    val errorMessage: String?
        get() = saveErrorMessage ?: historyErrorMessage

    /**
     * `true` mientras el diálogo de confirmación de borrado está visible.
     * El ViewModel conserva el id seleccionado para que la UI no duplique estado.
     */
    val isDeleteDialogVisible: Boolean
        get() = pendingDeletionId != null

    val isDeleting: Boolean
        get() = deleteState is Result.Loading

    val deleteError: Result.Error?
        get() = deleteState as? Result.Error

    val deleteErrorMessage: String?
        get() = deleteError?.message
}
