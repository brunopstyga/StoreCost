package com.tickets.store_cost.domain.model

/**
 * Reporte mensual de ventas generado en formato CSV.
 *
 * @param fileName Nombre sugerido del archivo `.csv`.
 * @param content Contenido CSV completo (cabecera + filas).
 * @param salesCount Cantidad de ventas incluidas.
 * @param totalAmount Suma de los subtotales del período.
 */
data class MonthlyReport(
    val fileName: String,
    val content: String,
    val salesCount: Int,
    val totalAmount: Double
)
