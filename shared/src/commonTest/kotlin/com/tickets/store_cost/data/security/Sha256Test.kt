package com.tickets.store_cost.data.security

import kotlin.test.Test
import kotlin.test.assertEquals

class Sha256Test {

    @Test
    fun testKnownVectors() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            Sha256.hash("")
        )
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            Sha256.hash("abc")
        )
        assertEquals(
            "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
            Sha256.hash("hello")
        )
    }

    @Test
    fun testHashIsDeterministic() {
        assertEquals(Sha256.hash("1234"), Sha256.hash("1234"))
    }
}
