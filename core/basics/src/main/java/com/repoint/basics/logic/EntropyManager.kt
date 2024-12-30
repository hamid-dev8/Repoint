package com.repoint.basics.logic

import android.util.Log
import org.web3j.crypto.Hash
import org.web3j.crypto.MnemonicUtils
import org.web3j.utils.Numeric
import java.nio.ByteBuffer
import java.security.SecureRandom

object EntropyManager {
    private val entropy_bytes_16 = 16
    val entropy_bytes_32 = 32
    val ethereumTag = "ETH"

    fun generateEntropy(entropyBytes: Int = 16): ByteArray {
        val random = SecureRandom()
        val entropy: ByteArray = ByteArray(entropyBytes)
        random.nextBytes(entropy)
        return entropy
    }


    private fun generateDeterministicEntropy(seed: String): ByteArray {

        val seedBytes: ByteArray = seed.toByteArray()
        return Hash.sha3(seedBytes)
    }

    private fun entropyToHexString(entropy: ByteArray): String =
        Numeric.toHexStringNoPrefix(entropy)

    private fun generateCombinedEntropy(randomEntropy: ByteArray, additionalInfo: String): ByteArray {

        val additionalInfoBytes: ByteArray = additionalInfo.toByteArray()
        val combinedBuffer = ByteBuffer.allocate(randomEntropy.size + additionalInfoBytes.size)
        combinedBuffer.put(randomEntropy)
        combinedBuffer.put(additionalInfoBytes)
        return Hash.sha3(combinedBuffer.array())
    }

    fun getPhrase() : String {

        val entropy = generateEntropy(entropy_bytes_16)
        Log.d(ethereumTag, "just entropy : $entropy")
        Log.d(ethereumTag, "Random Entropy (HEX) : " + entropyToHexString(entropy))
        val phrase = MnemonicUtils.generateMnemonic(entropy)
        Log.d(ethereumTag, "phrase Entropy (HEX) :  $phrase")

        return phrase
    }
}