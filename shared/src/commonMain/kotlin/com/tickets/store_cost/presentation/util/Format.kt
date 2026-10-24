package com.tickets.store_cost.presentation.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Formatea un monto como moneda con separador de miles y dos decimales.
 * Ej: 1210.0 -> "$1.210,00"
 */
fun formatCurrency(value: Double): String {
    val cents = (value * 100).roundToLong()
    val sign = if (cents < 0) "-" else ""
    val absolute = abs(cents)
    val integerPart = absolute / 100
    val decimalPart = absolute % 100
    return sign + "$" + groupThousands(integerPart) + "," + decimalPart.toString().padStart(2, '0')
}

/**
 * Formatea un epoch en milisegundos como "dd/MM/yyyy HH:mm".
 */
fun formatTimestamp(timestamp: Long): String {
    val dateTime = Instant.fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val day = dateTime.dayOfMonth.toString().padStart(2, '0')
    val month = dateTime.monthNumber.toString().padStart(2, '0')
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "$day/$month/${dateTime.year} $hour:$minute"
}

/**
 * Formatea un rango [start, end] como "dd/MM/yyyy - dd/MM/yyyy".
 */
fun formatDateRange(startTime: Long, endTime: Long): String {
    val timeZone = TimeZone.currentSystemDefault()
    val start = Instant.fromEpochMilliseconds(startTime).toLocalDateTime(timeZone).date
    val end = Instant.fromEpochMilliseconds(endTime).toLocalDateTime(timeZone).date
    return "${formatDate(start)} - ${formatDate(end)}"
}

/**
 * Formatea el mes y año de un epoch en milisegundos usando el nombre de mes
 * provisto por la capa de UI.
 * Ej: 1792065600000 + "Octubre" -> "Octubre 2026"
 */
fun formatMonthYear(timestamp: Long, monthName: String): String {
    val date = Instant.fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
    return "$monthName ${date.year}"
}

private fun formatDate(date: kotlinx.datetime.LocalDate): String {
    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = date.monthNumber.toString().padStart(2, '0')
    return "$day/$month/${date.year}"
}

private fun groupThousands(value: Long): String {
    val text = value.toString()
    val builder = StringBuilder()
    text.forEachIndexed { index, char ->
        if (index > 0 && (text.length - index) % 3 == 0) builder.append('.')
        builder.append(char)
    }
    return builder.toString()
}
