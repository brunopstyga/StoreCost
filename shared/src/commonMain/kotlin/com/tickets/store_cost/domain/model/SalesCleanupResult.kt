package com.tickets.store_cost.domain.model

/**
 * Resultado de la purga quincenal: cantidad de ventas y cierres eliminados.
 */
data class SalesCleanupResult(
    val deletedSales: Int,
    val deletedClosings: Int
)
