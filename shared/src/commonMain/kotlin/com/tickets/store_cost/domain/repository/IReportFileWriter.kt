package com.tickets.store_cost.domain.repository

import com.tickets.store_cost.domain.model.MonthlyReport
import com.tickets.store_cost.domain.model.Result

/**
 * Contrato de escritura física del reporte. La implementación es específica
 * de cada plataforma (en Android escribe en el almacenamiento de la app).
 */
interface IReportFileWriter {
    /**
     * Persiste el reporte y devuelve la ruta absoluta del archivo generado.
     */
    suspend fun write(report: MonthlyReport): Result<String>
}
