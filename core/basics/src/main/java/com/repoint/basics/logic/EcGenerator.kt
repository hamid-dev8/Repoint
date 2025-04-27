package com.repoint.basics.logic

import com.repoint.models.sharedmodels.local.ChainWallet
import com.repoint.models.sharedmodels.local.MasterWallet
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object EcGenerator {
    private val supportedCoinType = listOf(60, 714, 966)

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

        val chainWallet = supportedCoinType.map { coinType ->
            val path = intArrayOf(
                44 or Bip32ECKeyPair.HARDENED_BIT,
                coinType or Bip32ECKeyPair.HARDENED_BIT,
                0 or Bip32ECKeyPair.HARDENED_BIT,
                0, 0
            )

            val childKeypair = Bip32ECKeyPair.deriveKeyPair(masterKeyPair, path)
            val credentials = Credentials.create(childKeypair)

            ChainWallet(
                masterWalletId = masterWallet.masterWalletId,
                coinType = coinType,
                networkName = getNetworkNameByCoinType(coinType),
                publicKey = childKeypair.publicKey.toString(),
                privateKey = childKeypair.privateKey.toString(),
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

        val chainWallets = supportedCoinType.map { coinType ->

            val path = intArrayOf(
                44 or Bip32ECKeyPair.HARDENED_BIT,
                coinType or Bip32ECKeyPair.HARDENED_BIT,
                0 or Bip32ECKeyPair.HARDENED_BIT,
                0 , 0
            )

            val childKeyPair = Bip32ECKeyPair.deriveKeyPair(masterKeyPair,path)
            val credentials = Credentials.create(childKeyPair)

            ChainWallet(
                masterWalletId = masterWallet.masterWalletId,
                coinType = coinType,
                networkName = getNetworkNameByCoinType(coinType),
                publicKey = childKeyPair.publicKey.toString(),
                privateKey = childKeyPair.privateKey.toString(),
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
        val binary = mnemonicToBinary(mnemonic, MnemonicUtils.getWords())
        val (entropy, checksum) = binaryToEntropy(binary)
        val calculatedChecksum = calculateChecksum(entropy)
        return calculatedChecksum == checksum
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