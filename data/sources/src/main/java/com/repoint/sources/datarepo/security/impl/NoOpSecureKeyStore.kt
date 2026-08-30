package com.repoint.sources.datarepo.security.impl

import com.repoint.sources.datarepo.security.SecureKeyStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpSecureKeyStore @Inject constructor() : SecureKeyStore {
    override fun encrypt(plainText: String): String = plainText
    override fun decrypt(cipherText: String): String = cipherText
}