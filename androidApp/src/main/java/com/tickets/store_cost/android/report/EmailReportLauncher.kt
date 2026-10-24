package com.tickets.store_cost.android.report

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.tickets.store_cost.android.R
import java.io.File

/**
 * Abre el selector de aplicaciones de correo con el CSV adjunto mediante
 * `Intent.ACTION_SEND` y una URI `content://` provista por [FileProvider].
 */
object EmailReportLauncher {

    private const val MIME_TYPE = "text/csv"

    fun launch(context: Context, filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(
                context,
                context.getString(R.string.email_file_missing),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = MIME_TYPE
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.email_subject))
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(
                    sendIntent,
                    context.getString(R.string.email_chooser_title)
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.email_open_failed, e.message ?: ""),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
