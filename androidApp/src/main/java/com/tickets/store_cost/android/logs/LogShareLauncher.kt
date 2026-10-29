package com.tickets.store_cost.android.logs

import android.content.Context
import com.tickets.store_cost.android.R
import com.tickets.store_cost.android.share.FileShareLauncher
import com.tickets.store_cost.core.logging.AppLogger

/**
 * Exporta el archivo local de registros de diagnóstico mediante el selector
 * nativo de Android (`Intent.ACTION_SEND` + `FileProvider`).
 *
 * La compartición se delega en [FileShareLauncher], el mismo helper usado por
 * los reportes CSV.
 */
object LogShareLauncher {

    private const val MIME_TYPE = "text/plain"

    fun shareLogFile(context: Context) {
        FileShareLauncher.shareFile(
            context = context,
            filePath = AppLogger.logFilePath(context),
            mimeType = MIME_TYPE,
            subject = context.getString(R.string.logs_subject),
            chooserTitle = context.getString(R.string.logs_chooser_title),
            missingFileMessage = context.getString(R.string.logs_file_missing)
        )
    }
}
