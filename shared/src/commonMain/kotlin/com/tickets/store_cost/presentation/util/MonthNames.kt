package com.tickets.store_cost.presentation.util

import androidx.compose.runtime.Composable
import com.tickets.store_cost.resources.Res
import com.tickets.store_cost.resources.month_april
import com.tickets.store_cost.resources.month_august
import com.tickets.store_cost.resources.month_december
import com.tickets.store_cost.resources.month_february
import com.tickets.store_cost.resources.month_january
import com.tickets.store_cost.resources.month_july
import com.tickets.store_cost.resources.month_june
import com.tickets.store_cost.resources.month_march
import com.tickets.store_cost.resources.month_may
import com.tickets.store_cost.resources.month_november
import com.tickets.store_cost.resources.month_october
import com.tickets.store_cost.resources.month_september
import kotlinx.datetime.Instant
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private fun monthNameRes(month: Month): StringResource = when (month) {
    Month.JANUARY -> Res.string.month_january
    Month.FEBRUARY -> Res.string.month_february
    Month.MARCH -> Res.string.month_march
    Month.APRIL -> Res.string.month_april
    Month.MAY -> Res.string.month_may
    Month.JUNE -> Res.string.month_june
    Month.JULY -> Res.string.month_july
    Month.AUGUST -> Res.string.month_august
    Month.SEPTEMBER -> Res.string.month_september
    Month.OCTOBER -> Res.string.month_october
    Month.NOVEMBER -> Res.string.month_november
    Month.DECEMBER -> Res.string.month_december
    else -> TODO()
}

/**
 * Variante composable de [formatMonthYear] que resuelve el nombre del mes
 * desde los recursos de UI.
 */
@Composable
fun formatMonthYear(timestamp: Long): String {
    val month = Instant.fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .month
    return formatMonthYear(timestamp, stringResource(monthNameRes(month)))
}
