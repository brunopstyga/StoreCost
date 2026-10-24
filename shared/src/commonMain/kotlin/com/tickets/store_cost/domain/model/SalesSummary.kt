package com.tickets.store_cost.domain.model

data class SalesSummary(
    val totalBilled: Double,
    val totalSalesCount: Int,
    val breakdown: Map<OperationType, Double>,
    val sales: List<Sale> = emptyList()
)
