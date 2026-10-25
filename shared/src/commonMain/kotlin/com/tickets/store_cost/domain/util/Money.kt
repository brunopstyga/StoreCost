package com.tickets.store_cost.domain.util

import kotlin.math.round

/**
 * Redondea un monto a dos decimales (centavos) para evitar arrastrar
 * errores de punto flotante en cálculos monetarios.
 */
fun Double.roundToCents(): Double = round(this * 100.0) / 100.0
