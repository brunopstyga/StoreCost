package com.tickets.store_cost.domain.util

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Fecha actual en la zona horaria indicada.
 */
fun todayIn(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    Clock.System.now().toLocalDateTime(timeZone).date

/**
 * Rango [inicio, fin] del día en epoch milisegundos.
 */
fun LocalDate.toDayRange(
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Pair<Long, Long> {
    val start = atStartOfDayIn(timeZone).toEpochMilliseconds()
    val end = atTime(23, 59, 59, 999_999_999)
        .toInstant(timeZone)
        .toEpochMilliseconds()
    return start to end
}

/**
 * Rango [inicio, fin] del mes que contiene esta fecha.
 */
fun LocalDate.toMonthRange(
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Pair<Long, Long> {
    val firstDay = LocalDate(year, month, 1)
    val lastDay = firstDay.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
    return firstDay.toDayRange(timeZone).first to lastDay.toDayRange(timeZone).second
}
