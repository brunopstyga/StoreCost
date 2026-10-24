package com.tickets.store_cost.data.mapper

import com.tickets.store_cost.data.local.entity.DailyClosingEntity
import com.tickets.store_cost.domain.model.DailyClosing

fun DailyClosingEntity.toDomain(): DailyClosing = DailyClosing(
    id = id,
    closingDate = closingDate,
    totalAmount = totalAmount,
    salesCount = salesCount,
    closedAt = closedAt
)

fun DailyClosing.toEntity(): DailyClosingEntity = DailyClosingEntity(
    id = id,
    closingDate = closingDate,
    totalAmount = totalAmount,
    salesCount = salesCount,
    closedAt = closedAt
)
