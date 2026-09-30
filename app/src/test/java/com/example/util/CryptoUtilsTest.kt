package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CryptoUtilsTest {

    @Test
    fun testMd5Generation() {
        val timestamp = "1785266317000"
        val key = "Kumi.2026_!N#"
        val input = timestamp + key

        val result = CryptoUtils.md5(input)

        // Verificar que el hash sea de 32 caracteres hexadecimales
        assertEquals(32, result.length)
        
        // Verificar contra el resultado esperado de MD5("1785266317000Kumi.2026_!N#")
        // java.security.MessageDigest md5 de "1785266317000Kumi.2026_!N#"
        val expected = java.security.MessageDigest.getInstance("MD5")
            .digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

        assertEquals(expected, result)
    }
}
