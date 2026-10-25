package com.tickets.store_cost.domain.model

import kotlin.coroutines.cancellation.CancellationException

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()
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
    errorPrefix: String,
    block: suspend () -> T
): Result<T> = try {
    Result.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.Error("$errorPrefix: ${e.message}", e)
}
