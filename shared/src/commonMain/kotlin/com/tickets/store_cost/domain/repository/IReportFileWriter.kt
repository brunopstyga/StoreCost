package com.tickets.store_cost.domain.repository

import com.tickets.store_cost.domain.model.MonthlyReport
import com.tickets.store_cost.domain.model.Result

/**
 * Contrato de escritura física de reportes. La implementación es específica
 * de cada plataforma (en Android escribe en el almacenamiento de la app).
 */
interface IReportFileWriter {
    /**
     * Persiste el reporte mensual y devuelve la ruta absoluta del archivo.
     */
    suspend fun write(report: MonthlyReport): Result<String> =
        write(report.fileName, report.content)

    /**
     * Persiste un archivo CSV genérico (por ejemplo, el respaldo quincenal)
     * y devuelve la ruta absoluta del archivo generado.
     */
    suspend fun write(fileName: String, content: String): Result<String>
}
