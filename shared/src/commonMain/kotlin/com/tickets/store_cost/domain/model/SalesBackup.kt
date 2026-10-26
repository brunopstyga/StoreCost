package com.tickets.store_cost.domain.model

/**
 * Respaldo quincenal de ventas y cierres generado antes de aplicar la política
 * de retención de 15 días.
 *
 * @param fileName Nombre sugerido del archivo `.csv`.
 * @param content Contenido completo (secciones de ventas y cierres).
 * @param salesCount Cantidad de ventas incluidas.
 * @param closingsCount Cantidad de cierres incluidos.
 * @param totalAmount Suma de los subtotales de las ventas respaldadas.
 */
data class SalesBackup(
    val fileName: String,
    val content: String,
    val salesCount: Int,
    val closingsCount: Int,
    val totalAmount: Double
)
