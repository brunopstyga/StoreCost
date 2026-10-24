package com.tickets.store_cost.data.repository

import com.tickets.store_cost.domain.model.DailyClosing
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.repository.IClosingRepository

class FakeClosingRepository(
    initialClosings: List<DailyClosing> = emptyList()
) : IClosingRepository {

    private val closings = initialClosings.associateBy { it.closingDate }.toMutableMap()
    private var nextId = (initialClosings.maxOfOrNull { it.id } ?: 0L) + 1L

    var shouldReturnError: Boolean = false

    val savedClosings: List<DailyClosing>
        get() = closings.values.toList()

    override suspend fun saveClosing(closing: DailyClosing): Result<Unit> {
        if (shouldReturnError) return Result.Error("Database error")

        val stored = closing.copy(id = nextId++)
        closings[stored.closingDate] = stored
        return Result.Success(Unit)
    }

    override suspend fun getClosingByDate(date: String): Result<DailyClosing?> {
        if (shouldReturnError) return Result.Error("Database error")
        return Result.Success(closings[date])
    }
}
