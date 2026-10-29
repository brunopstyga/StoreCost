package com.tickets.store_cost.android.share

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.tickets.store_cost.android.R
import com.tickets.store_cost.core.logging.AppLogger
import java.io.File

/**
 * Helper único para compartir archivos internos de la app mediante
 * `Intent.ACTION_SEND` y una URI `content://` provista por `FileProvider`.
 *
 * Se reutiliza tanto para los reportes CSV (Tarea 03/07) como para el archivo
 * de logs de diagnóstico (Tarea 09), evitando duplicar la lógica de
 * compartición (principio DRY).
 */
object FileShareLauncher {

    /**
     * Abre el selector nativo de aplicaciones para compartir [filePath].
     *
     * @param mimeType tipo MIME del archivo (`text/csv`, `text/plain`, etc.).
     * @param subject asunto precargado del correo.
     * @param chooserTitle título del selector de aplicaciones.
     * @param missingFileMessage mensaje mostrado si el archivo no existe.
     * @return `true` si se lanzó el selector; `false` si el archivo no existe
     *   o no se pudo abrir.
     */
    fun shareFile(
        context: Context,
        filePath: String,
        mimeType: String,
        subject: String,
        chooserTitle: String,
        missingFileMessage: String
    ): Boolean {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(context, missingFileMessage, Toast.LENGTH_LONG).show()
            return false
        }

        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(sendIntent, chooserTitle)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            true
        } catch (e: Exception) {
            AppLogger.e("No se pudo compartir el archivo $filePath", e)
            Toast.makeText(
                context,
                context.getString(R.string.share_open_failed, e.message ?: ""),
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }
}
