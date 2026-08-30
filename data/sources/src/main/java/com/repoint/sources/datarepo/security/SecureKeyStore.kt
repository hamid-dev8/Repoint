package com.repoint.sources.datarepo.security

interface SecureKeyStore {
    fun encrypt(plainText: String): String
    fun decrypt(cipherText: String): String
}
