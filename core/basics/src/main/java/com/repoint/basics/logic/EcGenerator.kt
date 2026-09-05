package com.repoint.basics.logic

import android.util.Log
import com.repoint.models.sharedmodels.local.ChainWallet
import com.repoint.models.sharedmodels.local.MasterWallet
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils
import org.web3j.utils.Numeric
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object EcGenerator {
    private val sharedEvmNetworks = listOf(
        "Ethereum",
        "Polygon",
        "Arbitrum",
        "Optimism",
        "Avalanche",
        "Fantom"
    )
    private val supportedChainDefinitions = listOf(
        "Ethereum" to 60,
        "Polygon" to 60,
        "Arbitrum" to 60,
        "Optimism" to 60,
        "Avalanche" to 60,
        "Fantom" to 60,
        "BNB Smart Chain" to 714
    )

    fun generateMasterAndChainWallets(
        walletName: String,
        userId: String? = null
    ): Pair<MasterWallet, List<ChainWallet>> {

        val phrase = EntropyManager.getPhrase()
        val seed = MnemonicUtils.generateSeed(phrase, "")
        val masterKeyPair = Bip32ECKeyPair.generateKeyPair(seed)
        val masterWallet = MasterWallet(
            userId = userId,
            phrase = phrase,
            name = walletName,
            creationDate = getCurrentDate()
        )

        val evmPath = intArrayOf(
            44 or Bip32ECKeyPair.HARDENED_BIT,
            60 or Bip32ECKeyPair.HARDENED_BIT,
            0 or Bip32ECKeyPair.HARDENED_BIT,
            0, 0
        )
        val sharedEvmKeyPair = Bip32ECKeyPair.deriveKeyPair(masterKeyPair, evmPath)
        val sharedEvmCredentials = Credentials.create(sharedEvmKeyPair)
        val sharedEvmPublicKeyHex = Numeric.toHexStringNoPrefixZeroPadded(sharedEvmKeyPair.publicKey, 128)
        val sharedEvmPrivateKey = sharedEvmKeyPair.privateKey.toString(16).padStart(64, '0')

        val chainWallet = supportedChainDefinitions.map { (networkName, coinType) ->
            val walletKeyPair = if (coinType == 60) {
                sharedEvmKeyPair
            } else {
                val path = intArrayOf(
                    44 or Bip32ECKeyPair.HARDENED_BIT,
                    coinType or Bip32ECKeyPair.HARDENED_BIT,
                    0 or Bip32ECKeyPair.HARDENED_BIT,
                    0, 0
                )
                Bip32ECKeyPair.deriveKeyPair(masterKeyPair, path)
            }
            val credentials = Credentials.create(walletKeyPair)
            val ecPublicKeyHex = Numeric.toHexStringNoPrefixZeroPadded(walletKeyPair.publicKey, 128)
            val privateKey = walletKeyPair.privateKey.toString(16).padStart(64, '0')

            ChainWallet(
                masterWalletId = masterWallet.masterWalletId,
                coinType = coinType,
                networkName = networkName,
                publicKey = ecPublicKeyHex,
                privateKey = privateKey,
                address = credentials.address
            )
        }

        return Pair(masterWallet, chainWallet)
    }

    fun importMasterAndChainWallets(
        mnemonic : String,
        walletName : String,
        userId : String? = null
    ) : Pair<MasterWallet,List<ChainWallet>>{

        val seed = MnemonicUtils.generateSeed(mnemonic,"")
        val masterKeyPair = Bip32ECKeyPair.generateKeyPair(seed)

        val masterWallet = MasterWallet(
            userId = userId,
            phrase = mnemonic,
            name = walletName,
            creationDate = getCurrentDate()
        )

        val evmPath = intArrayOf(
            44 or Bip32ECKeyPair.HARDENED_BIT,
            60 or Bip32ECKeyPair.HARDENED_BIT,
            0 or Bip32ECKeyPair.HARDENED_BIT,
            0, 0
        )
        val sharedEvmKeyPair = Bip32ECKeyPair.deriveKeyPair(masterKeyPair, evmPath)

        val chainWallets = supportedChainDefinitions.map { (networkName, coinType) ->
            val walletKeyPair = if (coinType == 60) sharedEvmKeyPair else {
                val path = intArrayOf(
                    44 or Bip32ECKeyPair.HARDENED_BIT,
                    coinType or Bip32ECKeyPair.HARDENED_BIT,
                    0 or Bip32ECKeyPair.HARDENED_BIT,
                    0 , 0
                )
                Bip32ECKeyPair.deriveKeyPair(masterKeyPair,path)
            }
            val credentials = Credentials.create(walletKeyPair)
            val ecPublicKeyHex = Numeric.toHexStringNoPrefixZeroPadded(walletKeyPair.publicKey, 128)
            val privateKey = walletKeyPair.privateKey.toString(16).padStart(64, '0')

            Log.d("ecGen","the private key is : ${walletKeyPair.privateKey}")

            ChainWallet(
                masterWalletId = masterWallet.masterWalletId,
                coinType = coinType,
                networkName = networkName,
                publicKey = ecPublicKeyHex,
                privateKey = privateKey,
                address = credentials.address
            )
        }

        return Pair(masterWallet,chainWallets)
    }


    private fun getCurrentDate(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val createdAt = current.format(formatter)

        return createdAt
    }

    private fun getNetworkNameByCoinType(coinType: Int): String = when (coinType) {
        60 -> "Ethereum"
        714 -> "Binance Smart Chain"
        966 -> "Polygon"
        else -> "Unknown"
    }

    fun getCoinTypeByNetworkName(name: String): Int = when (name.lowercase()) {
        "ethereum" -> 60
        "binance smart chain", "bsc" -> 714
        "polygon" -> 966
        else -> -1
    }

    fun isValidMnemonic(mnemonic: String): Boolean {
        val normalizedMnemonic = normalizeMnemonic(mnemonic)
        if (normalizedMnemonic.isBlank()) return false

        val words = normalizedMnemonic.split(Regex("\\s+"))
        if (words.size !in listOf(12, 15, 18, 21, 24)) return false

        return try {
            MnemonicUtils.validateMnemonic(normalizedMnemonic)
        } catch (e: Exception) {
            false
        }
    }

    fun getMnemonicSuggestions(prefix: String): List<String> {
        val query = prefix.trim().lowercase()
        if (query.isEmpty()) return emptyList()
        return MnemonicUtils.getWords()
            .filter { it.startsWith(query) }
            .take(6)
    }

    private fun normalizeMnemonic(mnemonic: String): String {
        return mnemonic.trim().split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .joinToString(" ")
            .lowercase()
    }

    private fun mnemonicToBinary(mnemonic: String, wordList: List<String>): String {
        val words = mnemonic.split(" ")
        return words.joinToString("") { word ->
            val index = wordList.indexOf(word)
            String.format("%11s", Integer.toBinaryString(index)).replace(' ', '0')
        }
    }

    private fun binaryToEntropy(binary: String): Pair<String, String> {
        val checksumLength = binary.length / 33
        val entropy = binary.substring(0, binary.length - checksumLength)
        val checksum = binary.substring(binary.length - checksumLength)
        return Pair(entropy, checksum)
    }

    private fun calculateChecksum(entropy: String): String {
        // Convert entropy string to byte array
        val entropyBytes = entropy.chunked(8).map { it.toInt(2).toByte() }.toByteArray()

        // Calculate SHA-256 hash of the entropy
        val hash = MessageDigest.getInstance("SHA-256").digest(entropyBytes)

        // Calculate checksum length
        val checksumLength = entropy.length / 32

        // Extract checksum bits from the hash
        val checksum = hash[0].toInt() shr (8 - checksumLength) and ((1 shl checksumLength) - 1)

        // Convert checksum to binary string and pad it to checksumLength bits
        return Integer.toBinaryString(checksum).padStart(checksumLength, '0')
    }

}