package com.tickets.store_cost.core.logging

/**
 * Implementación iOS de [AppLogger]: escribe en la consola del sistema.
 *
 * La exportación de archivos y el `FileProvider` son específicos de Android;
 * en iOS los registros quedan disponibles en la consola de Xcode.
 */
actual object AppLogger {

    actual fun d(message: String, throwable: Throwable?) = log("D", message, throwable)

    actual fun i(message: String, throwable: Throwable?) = log("I", message, throwable)

    actual fun w(message: String, throwable: Throwable?) = log("W", message, throwable)

    actual fun e(message: String, throwable: Throwable?) = log("E", message, throwable)

    private fun log(level: String, message: String, throwable: Throwable?) {
        val entry = buildString {
            append('[')
            append(level)
            append("] ")
            append(message)
            if (throwable != null) {
                append('\n')
                append(throwable.stackTraceToString())
            }
        }
        println(entry)
    }
}
