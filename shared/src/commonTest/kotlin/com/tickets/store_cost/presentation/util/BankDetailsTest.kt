package com.tickets.store_cost.presentation.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * El QR bancario debe contener únicamente el alias en texto plano para que el
 * cliente lo copie desde el lector. No debe incluir URLs, esquemas ni
 * parámetros de ningún tipo.
 */
class BankDetailsTest {

    @Test
    fun testQrPayloadEsElAliasEnTextoPlano() {
        assertEquals(BankDetails.ALIAS, BankDetails.QR_PAYLOAD)
    }

    @Test
    fun testQrPayloadNoContieneUrlNiEsquema() {
        val payload = BankDetails.QR_PAYLOAD

        assertFalse(payload.contains("://"), "El QR no debe contener un esquema de URI")
        assertFalse(payload.startsWith("http", ignoreCase = true), "El QR no debe ser una URL")
        assertFalse(payload.contains("www."), "El QR no debe ser una dirección web")
        assertFalse(payload.contains("?"), "El QR no debe contener query string")
        assertFalse(payload.contains("="), "El QR no debe contener parámetros")
    }
}
