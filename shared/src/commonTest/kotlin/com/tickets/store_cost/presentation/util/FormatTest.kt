package com.tickets.store_cost.presentation.util

import com.tickets.store_cost.core.util.formatDate
import com.tickets.store_cost.domain.util.todayIn
import com.tickets.store_cost.domain.util.toDayRange
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatTest {

    @Test
    fun testFormatCurrencyWithThousandsSeparator() {
        assertEquals("\$1.210,00", formatCurrency(1210.0))
        assertEquals("\$2.560,00", formatCurrency(2560.0))
        assertEquals("\$2.420,00", formatCurrency(2420.0))
    }

    @Test
    fun testFormatCurrencyWithDecimals() {
        assertEquals("\$1.105,50", formatCurrency(1105.5))
    }

    @Test
    fun testFormatCurrencyWithZero() {
        assertEquals("\$0,00", formatCurrency(0.0))
    }

    @Test
    fun testFormatMonthYear() {
        val timestamp = Instant.parse("2026-10-15T12:00:00Z").toEpochMilliseconds()
        assertEquals("Octubre 2026", formatMonthYear(timestamp, "Octubre"))
    }

    @Test
    fun testFormatDateRangeForASingleDayCollapsesToDate() {
        val (start, end) = todayIn().toDayRange()

        assertEquals(formatDate(todayIn()), formatDateRange(start, end))
    }
}
