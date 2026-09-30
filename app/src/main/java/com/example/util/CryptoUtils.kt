package com.example.util

import java.security.MessageDigest

object CryptoUtils {
    /**
     * Calcula el hash MD5 en formato Hexadecimal (32 caracteres minúsculas)
     * para una cadena dada de entrada.
     */
    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
