package com.tickets.store_cost.data.report

import android.content.Context
import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.repository.IReportFileWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

/**
 * Escribe el CSV en el directorio de caché de la app (`cache/reports`).
 * Ese directorio está expuesto mediante `FileProvider` para adjuntarlo al correo.
 */
@Single(binds = [IReportFileWriter::class])
class AndroidReportFileWriter(
    private val context: Context
) : IReportFileWriter {

    override suspend fun write(fileName: String, content: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val directory = File(context.cacheDir, REPORTS_DIRECTORY).apply { mkdirs() }
                val file = File(directory, fileName)
                file.writeText(content)
                Result.Success(file.absolutePath)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.Error(ErrorCode.SAVE_REPORT_FAILED, e.message)
            }
        }

    private companion object {
        const val REPORTS_DIRECTORY = "reports"
    }
}
