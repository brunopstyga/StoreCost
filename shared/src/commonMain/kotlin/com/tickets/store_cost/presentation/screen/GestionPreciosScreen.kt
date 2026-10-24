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
import androidx.compose.material3.MaterialTheme
import com.tickets.store_cost.presentation.screen.components.AppOutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.PriceConsultation
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.presentation.state.ConsultingUiState
import com.tickets.store_cost.presentation.util.displayName
import com.tickets.store_cost.presentation.util.formatCurrency
import com.tickets.store_cost.presentation.util.formatTimestamp
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

@OptIn(ExperimentalMaterial3Api::class)
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppOutlinedTextField(
            value = state.basePrice,
            onValueChange = onBasePriceChange,
            label = stringResource(Res.string.prices_base_price_label),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        AppOutlinedTextField(
            value = state.quantity,
            onValueChange = onQuantityChange,
            label = stringResource(Res.string.common_quantity_label),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Text(
            text = stringResource(Res.string.prices_select_modality),
            style = MaterialTheme.typography.titleMedium
        )

        ModalityDropdown(
            selected = state.selectedModality,
            onModalitySelected = onModalitySelected
        )

        Text(
            text = stringResource(Res.string.prices_calculated_list),
            style = MaterialTheme.typography.titleMedium
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            ConsultationList(consultations = state.consultations)
        }

        state.saveErrorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (state.isSaved) {
            Text(
                text = stringResource(Res.string.prices_saved_success),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (state.saveState == null) {
            state.historyErrorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
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
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = consultations.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        is Result.Success -> {
            val ordered = consultations.data.sortedWith(
                compareByDescending<PriceConsultation> { it.timestamp }
                    .thenByDescending { it.id }
            )
            if (ordered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.prices_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = ordered, key = { it.id }) { consultation ->
                        ConsultationRow(consultation = consultation)
                    }
                }
            }
        }
    }
}

@Composable
private fun modalityDisplayName(raw: String): String {
    val operationType = OperationType.entries.firstOrNull { it.name == raw }
    return operationType?.displayName() ?: raw
}

@Composable
private fun ConsultationRow(consultation: PriceConsultation) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(
                    Res.string.prices_consultation_modality,
                    modalityDisplayName(consultation.selectedModality)
                ),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = formatCurrency(consultation.calculatedSubtotal),
                style = MaterialTheme.typography.titleSmall
            )
        }
        Text(
            text = stringResource(
                Res.string.prices_consultation_detail,
                consultation.quantity,
                formatCurrency(consultation.calculatedUnitPrice),
                formatCurrency(consultation.basePrice)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatTimestamp(consultation.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
