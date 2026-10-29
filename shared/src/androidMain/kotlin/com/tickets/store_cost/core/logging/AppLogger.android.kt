package com.tickets.store_cost.core.logging

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Implementación Android de [AppLogger].
 *
 * Mantiene un único archivo interno (`files/logs/app_logs.txt`) que se escribe
 * de forma sincronizada junto a Logcat. Para acotar el espacio en disco:
 *
 * - Si el archivo alcanza **1 MB** se recrea (vacía) antes de la siguiente
 *   escritura.
 * - Un archivo auxiliar (`app_logs.meta`) guarda el timestamp de creación; al
 *   superar **7 días** el archivo se vacía y se renueva el timestamp.
 *
 * El `init` se invoca una sola vez en `StoreCostApplication`.
 */
actual object AppLogger {

    private const val TAG = "StoreCost"
    private const val LOG_DIRECTORY = "logs"
    private const val LOG_FILE_NAME = "app_logs.txt"
    private const val META_FILE_NAME = "app_logs.meta"
    private const val TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS"

    /** Tamaño máximo permitido antes de recrear el archivo (1 MB). */
    private const val MAX_FILE_BYTES = 1L * 1024L * 1024L

    /** Antigüedad máxima del archivo antes de purgarlo (7 días). */
    private const val MAX_FILE_AGE_MS = 7L * 24L * 60L * 60L * 1000L

    private val lock = Any()
    private var logFile: File? = null
    private var metaFile: File? = null

    /**
     * Habilita la escritura en archivo. Debe llamarse en el arranque de la app
     * con el contexto de la aplicación (no de una Activity).
     */
    fun init(context: Context) {
        synchronized(lock) {
            val directory = File(context.applicationContext.filesDir, LOG_DIRECTORY)
            if (!directory.exists()) directory.mkdirs()

            logFile = File(directory, LOG_FILE_NAME)
            metaFile = File(directory, META_FILE_NAME)
            ensureFreshFile()
            append("I", "AppLogger inicializado", null)
        }
    }

    /**
     * Ruta absoluta del archivo de registros, requerida por `FileProvider`
     * para compartir el diagnóstico desde la UI.
     */
    fun logFilePath(context: Context): String =
        File(File(context.applicationContext.filesDir, LOG_DIRECTORY), LOG_FILE_NAME)
            .absolutePath

    actual fun d(message: String, throwable: Throwable?) = log("D", message, throwable)

    actual fun i(message: String, throwable: Throwable?) = log("I", message, throwable)

    actual fun w(message: String, throwable: Throwable?) = log("W", message, throwable)

    actual fun e(message: String, throwable: Throwable?) = log("E", message, throwable)

    private fun log(level: String, message: String, throwable: Throwable?) {
        synchronized(lock) {
            // Antes de `init` no hay destino de archivo; se evita tocar Logcat
            // para no romper las pruebas unitarias de JVM, donde android.util.Log
            // no está disponible.
            if (logFile == null) return

            Log.println(priorityOf(level), TAG, consoleMessage(message, throwable))
            ensureFreshFile()
            append(level, message, throwable)
        }
    }

    /**
     * Recrea el archivo cuando expiró (7 días) o superó el tamaño máximo.
     * El timestamp de creación se conserva en un archivo auxiliar para no
     * confundirlo con la última fecha de escritura.
     */
    private fun ensureFreshFile() {
        val file = logFile ?: return
        val meta = metaFile ?: return
        val now = System.currentTimeMillis()
        val createdAt = runCatching { meta.readText().trim().toLong() }.getOrNull()
        val expired = createdAt != null && now - createdAt >= MAX_FILE_AGE_MS
        val oversized = file.exists() && file.length() >= MAX_FILE_BYTES

        if (createdAt == null) {
            // Primer uso (o metadato ilegible): se sella la creación sin borrar
            // registros previos. Así un fallo de escritura del meta no provoca
            // que el archivo se vacíe en cada entrada.
            file.parentFile?.mkdirs()
            if (!file.exists()) file.createNewFile()
            runCatching { meta.writeText(now.toString()) }
            return
        }

        if (expired || oversized) {
            if (file.exists()) file.delete()
            file.parentFile?.mkdirs()
            file.createNewFile()
            meta.writeText(now.toString())
        }
    }

    private fun append(level: String, message: String, throwable: Throwable?) {
        val file = logFile ?: return
        val timestamp = SimpleDateFormat(TIMESTAMP_PATTERN, Locale.US).format(Date())
        val entry = buildString {
            append(timestamp)
            append(" [")
            append(level)
            append("] ")
            append(message)
            if (throwable != null) {
                append('\n')
                append(throwable.stackTraceToString())
            }
            append('\n')
        }
        runCatching { file.appendText(entry) }
            .onFailure { Log.println(Log.ERROR, TAG, "No se pudo escribir el log: ${it.message}") }
    }

    private fun consoleMessage(message: String, throwable: Throwable?): String =
        if (throwable == null) message else "$message\n${throwable.stackTraceToString()}"

    private fun priorityOf(level: String): Int = when (level) {
        "D" -> Log.DEBUG
        "I" -> Log.INFO
        "W" -> Log.WARN
        else -> Log.ERROR
    }
}
