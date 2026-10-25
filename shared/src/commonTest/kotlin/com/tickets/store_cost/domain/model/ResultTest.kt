package com.tickets.store_cost.domain.model

import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ResultTest {

    @Test
    fun testResultOfWrapsBusinessExceptions() = runTest {
        val result = resultOf(ErrorCode.UNKNOWN) {
            error("fallo simulado")
        }

        val error = assertIs<Result.Error>(result)
        assertEquals(ErrorCode.UNKNOWN, error.code)
        assertEquals("fallo simulado", error.detail)
    }

    @Test
    fun testResultOfRethrowsCancellation() = runTest {
        var thrown: Throwable? = null
        try {
            resultOf(ErrorCode.UNKNOWN) {
                throw CancellationException("cancelado")
            }
        } catch (e: Throwable) {
            thrown = e
        }

        assertIs<CancellationException>(thrown)
    }
}
