package com.tickets.store_cost.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.PriceConsultation
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.usecase.CalculatePriceUseCase
import com.tickets.store_cost.domain.usecase.GetPriceConsultationsUseCase
import com.tickets.store_cost.domain.usecase.SavePriceConsultationUseCase
import com.tickets.store_cost.presentation.state.ConsultingUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.android.annotation.KoinViewModel

/**
 * ViewModel UDF de la pantalla de Gestión de Precios.
 * Combina el estado del formulario con el flujo frío del historial de consultas
 * y lo transforma en un [StateFlow] cálido de [ConsultingUiState].
 */
@KoinViewModel
class ConsultingViewModel(
    private val calculatePriceUseCase: CalculatePriceUseCase,
    private val savePriceConsultationUseCase: SavePriceConsultationUseCase,
    private val getPriceConsultationsUseCase: GetPriceConsultationsUseCase
) : ViewModel() {

    private data class FormState(
        val basePrice: String = "1000.0",
        val quantity: String = "1",
        val selectedModality: OperationType = OperationType.FACTURA_A,
        val saveState: Result<Unit>? = null
    )

    private val formState = MutableStateFlow(FormState())

    val uiState: StateFlow<ConsultingUiState> =
        combine(formState, getPriceConsultationsUseCase()) { form, consultations ->
            ConsultingUiState(
                basePrice = form.basePrice,
                quantity = form.quantity,
                selectedModality = form.selectedModality,
                consultations = consultations,
                saveState = form.saveState
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ConsultingUiState()
        )

    fun onBasePriceChange(value: String) {
        formState.update { it.copy(basePrice = value, saveState = null) }
    }

    fun onQuantityChange(value: String) {
        formState.update { it.copy(quantity = value, saveState = null) }
    }

    fun onModalitySelected(modality: OperationType) {
        formState.update { it.copy(selectedModality = modality, saveState = null) }
    }

    /**
     * Calcula el precio de la modalidad seleccionada y lo persiste. El listado
     * de la UI se alimenta del flujo del historial, por lo que cada consulta
     * nueva se suma a las anteriores sin reemplazarlas.
     */
    fun onConsultPrices() {
        val current = formState.value
        val basePrice = current.basePrice.toDoubleOrNull()
        val quantity = current.quantity.toIntOrNull()

        if (basePrice == null || basePrice <= 0.0 || quantity == null || quantity <= 0) {
            formState.update {
                it.copy(
                    saveState = Result.Error(ErrorCode.INVALID_BASE_PRICE_AND_QUANTITY)
                )
            }
            return
        }

        viewModelScope.launch {
            formState.update { it.copy(saveState = Result.Loading) }

            val calculation = calculatePriceUseCase.calculate(
                basePrice = basePrice,
                quantity = quantity,
                operationType = current.selectedModality
            )

            val result = savePriceConsultationUseCase(
                PriceConsultation(
                    basePrice = basePrice,
                    quantity = quantity,
                    selectedModality = calculation.operationType.name,
                    calculatedUnitPrice = calculation.unitPrice,
                    calculatedSubtotal = calculation.subtotal,
                    timestamp = Clock.System.now().toEpochMilliseconds()
                )
            )
            formState.update { it.copy(saveState = result) }
        }
    }
}
