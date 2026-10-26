package com.tickets.store_cost.domain.util

import com.tickets.store_cost.core.util.formatDateTime
import com.tickets.store_cost.domain.model.DailyClosing
import com.tickets.store_cost.domain.model.Sale
import kotlinx.datetime.TimeZone
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Utilidades puras para serializar los datos de ventas y cierres en CSV.
 *
 * Se comparten entre el reporte mensual y el respaldo quincenal para evitar
 * duplicar el formato (DRY) y garantizar que ambos archivos sean consistentes.
 */
const val SALES_CSV_HEADER = "ID,Fecha,Modalidad,PrecioUnitario,Cantidad,Subtotal"
const val CLOSINGS_CSV_HEADER = "ID,Fecha,Total,Operaciones,CerradoEn"

fun buildSalesCsv(
    sales: List<Sale>,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String = buildString {
    appendLine(SALES_CSV_HEADER)
    sales.forEach { sale ->
        appendLine(
            listOf(
                sale.id.toString(),
                formatDateTime(sale.timestamp, timeZone),
                escapeCsv(sale.operationType.name),
                formatCsvDecimal(sale.unitPrice),
                sale.quantity.toString(),
                formatCsvDecimal(sale.subtotal)
            ).joinToString(",")
        )
    }
}

fun buildClosingsCsv(
    closings: List<DailyClosing>,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String = buildString {
    appendLine(CLOSINGS_CSV_HEADER)
    closings.forEach { closing ->
        appendLine(
            listOf(
                closing.id.toString(),
                escapeCsv(closing.closingDate),
                formatCsvDecimal(closing.totalAmount),
                closing.salesCount.toString(),
                formatDateTime(closing.closedAt, timeZone)
            ).joinToString(",")
        )
    }
}

/**
 * Formatea un importe con dos decimales y punto como separador, sin depender
 * del locale de la plataforma (requisito de interoperabilidad del CSV).
 */
fun formatCsvDecimal(value: Double): String {
    val cents = (value * 100).roundToLong()
    val sign = if (cents < 0) "-" else ""
    val absolute = abs(cents)
    return "$sign${absolute / 100}.${(absolute % 100).toString().padStart(2, '0')}"
}

/**
 * Escapa un campo según RFC 4180 cuando contiene coma, comillas o saltos.
 */
fun escapeCsv(value: String): String =
    if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
        "\"" + value.replace("\"", "\"\"") + "\""
    } else {
        value
    }
