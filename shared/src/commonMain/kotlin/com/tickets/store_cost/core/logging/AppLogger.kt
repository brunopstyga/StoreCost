package com.tickets.store_cost.core.logging

/**
 * Logger centralizado de la aplicación.
 *
 * Expone una API común para registrar diagnósticos desde cualquier capa
 * (dominio, datos y presentación) sin acoplarla a una plataforma concreta:
 *
 * - **Android**: escribe simultáneamente en Logcat y en el archivo interno
 *   `app_logs.txt` (ver `AppLogger.android.kt`), con límite de 1 MB y purga
 *   automática cada 7 días.
 * - **iOS**: escribe en la consola del sistema.
 *
 * Los errores técnicos deben registrarse con [e] **antes** de convertirlos en
 * un `Result.Error`, de modo que el stacktrace completo quede en el archivo de
 * diagnóstico y no en el estado inmutable que consume la UI.
 */
expect object AppLogger {

    /** Registro de depuración. */
    fun d(message: String, throwable: Throwable? = null)

    /** Registro informativo. */
    fun i(message: String, throwable: Throwable? = null)

    /** Registro de advertencia. */
    fun w(message: String, throwable: Throwable? = null)

    /**
     * Registro de error. [throwable] es opcional: cuando se dispone de la
     * excepción original se preserva su stacktrace completo.
     */
    fun e(message: String, throwable: Throwable? = null)
}
