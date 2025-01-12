package com.repoint.basics.logic

import android.health.connect.datatypes.units.Length
import org.bouncycastle.jcajce.spec.PBKDF2KeySpec
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


const val ALOGIRTHM = "PBKDF2WithHmacSHA256"

fun generateSalt() : String{

        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hashPasscode(passcode : String , salt : String) : String{
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest((passcode + salt).toByteArray())
        return Base64.getEncoder().encodeToString(hashedBytes)
    }

    fun hashPasscodePBKDF2(passcode: String,salt: String,iterations : Int = 120000,keyLength: Int = 256) : String {
        val saltBytes = Base64.getDecoder().decode(salt)
        val spec = PBEKeySpec(passcode.toCharArray(),saltBytes,iterations,keyLength)
        val factory = SecretKeyFactory.getInstance(ALOGIRTHM)
        val hash = factory.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(hash)
    }