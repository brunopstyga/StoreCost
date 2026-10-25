package com.tickets.store_cost.domain.util

import com.tickets.store_cost.core.util.formatDate
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class DomainUtilsTest {

    @Test
    fun testRoundToCentsRoundsHalfUpToTwoDecimals() {
        assertEquals(1000.24, 1000.239.roundToCents(), 0.0001)
        assertEquals(1000.23, 1000.231.roundToCents(), 0.0001)
        assertEquals(1210.0, 1210.0.roundToCents(), 0.0001)
    }

    @Test
    fun testDayRangeCoversTheWholeDay() {
        val (start, end) = LocalDate(2026, 10, 15).toDayRange(TimeZone.UTC)

        assertEquals(
            Instant.parse("2026-10-15T00:00:00Z").toEpochMilliseconds(),
            start
        )
        assertEquals(
            Instant.parse("2026-10-15T23:59:59.999Z").toEpochMilliseconds(),
            end
        )
    }

    @Test
    fun testMonthRangeCoversTheWholeMonth() {
        val (start, end) = LocalDate(2026, 2, 10).toMonthRange(TimeZone.UTC)

        assertEquals(
            Instant.parse("2026-02-01T00:00:00Z").toEpochMilliseconds(),
            start
        )
        assertEquals(
            Instant.parse("2026-02-28T23:59:59.999Z").toEpochMilliseconds(),
            end
        )
    }

    @Test
    fun testFormatDateUsesDayMonthYearPattern() {
        assertEquals("15/10/2026", formatDate(LocalDate(2026, 10, 15)))
    }
}
