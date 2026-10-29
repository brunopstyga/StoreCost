package com.tickets.store_cost.android.report

import android.content.Context
import com.tickets.store_cost.android.R
import com.tickets.store_cost.android.share.FileShareLauncher

/**
 * Abre el selector de aplicaciones de correo con el CSV adjunto.
 *
 * La compartición real vive en [FileShareLauncher], compartida con la
 * exportación de logs de diagnóstico.
 */
object EmailReportLauncher {

    private const val MIME_TYPE = "text/csv"

    fun launch(context: Context, filePath: String) {
        FileShareLauncher.shareFile(
            context = context,
            filePath = filePath,
            mimeType = MIME_TYPE,
            subject = context.getString(R.string.email_subject),
            chooserTitle = context.getString(R.string.email_chooser_title),
            missingFileMessage = context.getString(R.string.email_file_missing)
        )
    }
}
