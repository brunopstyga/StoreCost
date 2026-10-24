package com.tickets.store_cost.presentation.state

import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.PriceConsultation
import com.tickets.store_cost.domain.model.Result

/**
 * Estado inmutable de la pantalla de Gestión de Precios.
 * Encapsula los valores del formulario, el historial de consultas calculadas
 * y los estados [Result] de la carga del historial y del guardado.
 */
data class ConsultingUiState(
    val basePrice: String = "1000.0",
    val quantity: String = "1",
    val selectedModality: OperationType = OperationType.FACTURA_A,
    val consultations: Result<List<PriceConsultation>> = Result.Loading,
    val saveState: Result<Unit>? = null
) {
    val isSaving: Boolean
        get() = saveState is Result.Loading

    val isSaved: Boolean
        get() = saveState is Result.Success

    val saveErrorMessage: String?
        get() = (saveState as? Result.Error)?.message

    val historyErrorMessage: String?
        get() = (consultations as? Result.Error)?.message

    val errorMessage: String?
        get() = saveErrorMessage ?: historyErrorMessage
}
