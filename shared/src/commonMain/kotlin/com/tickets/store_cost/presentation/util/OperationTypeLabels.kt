package com.tickets.store_cost.presentation.util

import androidx.compose.runtime.Composable
import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.resources.Res
import com.tickets.store_cost.resources.operation_efectivo_negro
import com.tickets.store_cost.resources.operation_factura_a
import com.tickets.store_cost.resources.operation_factura_b
import com.tickets.store_cost.resources.operation_lote_100
import com.tickets.store_cost.resources.operation_lote_110
import org.jetbrains.compose.resources.stringResource

/**
 * Nombre legible de la modalidad de venta, resuelto desde los recursos de UI.
 */
@Composable
fun OperationType.displayName(): String = when (this) {
    OperationType.FACTURA_A -> stringResource(Res.string.operation_factura_a)
    OperationType.FACTURA_B -> stringResource(Res.string.operation_factura_b)
    OperationType.LOTE_100 -> stringResource(Res.string.operation_lote_100)
    OperationType.LOTE_110 -> stringResource(Res.string.operation_lote_110)
    OperationType.EFECTIVO_NEGRO -> stringResource(Res.string.operation_efectivo_negro)
}
