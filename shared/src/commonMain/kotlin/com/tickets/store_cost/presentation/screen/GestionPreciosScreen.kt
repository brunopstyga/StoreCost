package com.tickets.store_cost.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.PriceConsultation
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.presentation.screen.components.AppOutlinedTextField
import com.tickets.store_cost.presentation.screen.components.ErrorMessage
import com.tickets.store_cost.presentation.screen.components.SectionTitle
import com.tickets.store_cost.presentation.screen.components.SuccessMessage
import com.tickets.store_cost.presentation.state.ConsultingUiState
import com.tickets.store_cost.presentation.theme.AppStyles
import com.tickets.store_cost.presentation.util.displayName
import com.tickets.store_cost.presentation.util.displayMessage
import com.tickets.store_cost.presentation.util.formatCurrency
import com.tickets.store_cost.presentation.util.formatTimestamp
import com.tickets.store_cost.presentation.util.operationTypeDisplayName
import com.tickets.store_cost.presentation.viewmodel.ConsultingViewModel
import com.tickets.store_cost.resources.Res
import com.tickets.store_cost.resources.common_quantity_label
import com.tickets.store_cost.resources.prices_base_price_label
import com.tickets.store_cost.resources.prices_calculated_list
import com.tickets.store_cost.resources.prices_consult_button
import com.tickets.store_cost.resources.prices_consultation_detail
import com.tickets.store_cost.resources.prices_consultation_modality
import com.tickets.store_cost.resources.prices_empty
import com.tickets.store_cost.resources.prices_modality_label
import com.tickets.store_cost.resources.prices_saved_success
import com.tickets.store_cost.resources.prices_saving
import com.tickets.store_cost.resources.prices_select_modality
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pantalla de Gestión de Precios. Recolecta el [ConsultingUiState] del
 * [ConsultingViewModel] y delega el render a un contenido stateless.
 */
@Composable
fun GestionPreciosScreen(
    viewModel: ConsultingViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    GestionPreciosContent(
        state = state,
        onBasePriceChange = viewModel::onBasePriceChange,
        onQuantityChange = viewModel::onQuantityChange,
        onModalitySelected = viewModel::onModalitySelected,
        onConsultPrices = viewModel::onConsultPrices
    )
}

@Composable
private fun GestionPreciosContent(
    state: ConsultingUiState,
    onBasePriceChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onModalitySelected: (OperationType) -> Unit,
    onConsultPrices: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppStyles.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppStyles.Spacing.md)
    ) {
        val decimalKeyboard = remember { KeyboardOptions(keyboardType = KeyboardType.Decimal) }
        val numberKeyboard = remember { KeyboardOptions(keyboardType = KeyboardType.Number) }

        AppOutlinedTextField(
            value = state.basePrice,
            onValueChange = onBasePriceChange,
            label = stringResource(Res.string.prices_base_price_label),
            keyboardOptions = decimalKeyboard
        )

        AppOutlinedTextField(
            value = state.quantity,
            onValueChange = onQuantityChange,
            label = stringResource(Res.string.common_quantity_label),
            keyboardOptions = numberKeyboard
        )

        SectionTitle(text = stringResource(Res.string.prices_select_modality))

        ModalityDropdown(
            selected = state.selectedModality,
            onModalitySelected = onModalitySelected
        )

        SectionTitle(text = stringResource(Res.string.prices_calculated_list))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            ConsultationList(consultations = state.consultations)
        }

        state.saveError?.let { error ->
            ErrorMessage(error.displayMessage())
        }

        if (state.isSaved) {
            SuccessMessage(stringResource(Res.string.prices_saved_success))
        }

        if (state.saveState == null) {
            state.historyError?.let { error ->
                ErrorMessage(error.displayMessage())
            }
        }

        Button(
            onClick = onConsultPrices,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(if (state.isSaving) Res.string.prices_saving else Res.string.prices_consult_button))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModalityDropdown(
    selected: OperationType,
    onModalitySelected: (OperationType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        AppOutlinedTextField(
            value = selected.displayName(),
            onValueChange = {},
            readOnly = true,
            label = stringResource(Res.string.prices_modality_label),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            OperationType.entries.forEach { operationType ->
                DropdownMenuItem(
                    text = { Text(text = operationType.displayName()) },
                    onClick = {
                        onModalitySelected(operationType)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ConsultationList(consultations: Result<List<PriceConsultation>>) {
    when (consultations) {
        is Result.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        is Result.Error -> Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppStyles.Spacing.lg),
            contentAlignment = Alignment.Center
        ) {
            ErrorMessage(consultations.displayMessage())
        }

        is Result.Success -> {
            // El DAO ya entrega las consultas ordenadas por timestamp e id: la
            // composición no debe volver a ordenar en cada recomposición.
            if (consultations.data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppStyles.Spacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.prices_empty),
                        style = AppStyles.Typography.body,
                        color = AppStyles.Colors.secondaryText
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppStyles.Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(AppStyles.Spacing.md)
                ) {
                    items(items = consultations.data, key = { it.id }) { consultation ->
                        ConsultationRow(consultation = consultation)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsultationRow(consultation: PriceConsultation) {
    val subtotal = remember(consultation.id, consultation.calculatedSubtotal) {
        formatCurrency(consultation.calculatedSubtotal)
    }
    val unitPrice = remember(consultation.id, consultation.calculatedUnitPrice) {
        formatCurrency(consultation.calculatedUnitPrice)
    }
    val basePrice = remember(consultation.id, consultation.basePrice) {
        formatCurrency(consultation.basePrice)
    }
    val timestamp = remember(consultation.id, consultation.timestamp) {
        formatTimestamp(consultation.timestamp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppStyles.Spacing.hairline)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(
                    Res.string.prices_consultation_modality,
                    operationTypeDisplayName(consultation.selectedModality)
                ),
                style = AppStyles.Typography.itemTitle
            )
            Text(
                text = subtotal,
                style = AppStyles.Typography.itemTitle
            )
        }
        Text(
            text = stringResource(
                Res.string.prices_consultation_detail,
                consultation.quantity,
                unitPrice,
                basePrice
            ),
            style = AppStyles.Typography.caption,
            color = AppStyles.Colors.secondaryText
        )
        Text(
            text = timestamp,
            style = AppStyles.Typography.caption,
            color = AppStyles.Colors.secondaryText
        )
    }
}
