package com.tickets.store_cost.core.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Formatea un epoch en milisegundos como "dd/MM/yyyy HH:mm".
 */
fun formatDateTime(
    timestamp: Long,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val dateTime = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(timeZone)
    val day = dateTime.dayOfMonth.toString().padStart(2, '0')
    val month = dateTime.monthNumber.toString().padStart(2, '0')
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "$day/$month/${dateTime.year} $hour:$minute"
}

/**
 * Formatea una fecha como "dd/MM/yyyy".
 */
fun formatDate(date: LocalDate): String {
    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = date.monthNumber.toString().padStart(2, '0')
    return "$day/$month/${date.year}"
}
