package com.tickets.store_cost.domain.model

import kotlin.coroutines.cancellation.CancellationException

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * Error de negocio o de datos identificado por [code].
     *
     * [detail] conserva el mensaje técnico (por ejemplo, el de una excepción)
     * únicamente para logs y pruebas; la UI muestra el texto localizado que
     * corresponde a [code].
     */
    data class Error(
        val code: ErrorCode,
        val detail: String? = null,
        val exception: Throwable? = null
    ) : Result<Nothing>() {
        val message: String
            get() = detail?.takeIf { it.isNotBlank() } ?: code.name
    }

    data object Loading : Result<Nothing>()
}

fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    else -> null
}

/**
 * Ejecuta [block] y encapsula su resultado o excepción en [Result].
 *
 * Las excepciones de cancelación se propagan siempre: una corrutina cancelada
 * no debe convertirse en un error de negocio ni actualizar estado obsoleto.
 */
suspend fun <T> resultOf(
    code: ErrorCode,
    block: suspend () -> T
): Result<T> = try {
    Result.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.Error(code, e.message, e)
}
