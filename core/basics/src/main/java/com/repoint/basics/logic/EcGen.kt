package com.repoint.basics.logic

import android.util.Log
import com.repoint.models.sharedmodels.RepointWallet
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Bip39Wallet
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.WalletUtils
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EcGen() {

    private val ethereumNetwork = "Ethereum"

    companion object {
        val instance: EcGen by lazy { EcGen() }
        private val entropyManager: EntropyManager = EntropyManager
    }
    lateinit var wallet : RepointWallet
    fun getFromMnemonic(): RepointWallet {


            val phrase = entropyManager.getPhrase()
            val seedPhrase = MnemonicUtils.generateSeed(phrase, "")
            val masterKeyPairing: Bip32ECKeyPair = Bip32ECKeyPair.generateKeyPair(seedPhrase)
            val derivationPath =
                intArrayOf(44 or -0x80000000, 60 or -0x80000000, 0 or -0x80000000, 0, 0)
            val derivedKeyPair = Bip32ECKeyPair.deriveKeyPair(masterKeyPairing, derivationPath)
            val publicKey = masterKeyPairing.publicKey
            val hexOfIt = derivedKeyPair.privateKey.toString(16)
            val credentials = Credentials.create(derivedKeyPair)

            wallet = RepointWallet(
                phrase = phrase,
                name = "wallet",
                creationDate = getCurrentDate(),
                network = ethereumNetwork,
                publicKey = publicKey.toString(),
                privateKey = hexOfIt,
                address = credentials.address,
                balance = 0.0
            )


            //Log.d("ETH", "private key Hex = $hexOfIt")
            Log.d("ETH", "" +
                    "50 ==> seed phrase is = $phrase")
            Log.d("ETH", "" +
                    "50 ==> credentials = ${credentials.address}")

            return wallet

    }

    fun importWithMnemonic(
        mnemonic: String,
        passPhrase: String = "",
        walletName: String
    ): RepointWallet? {

        /*    if (isValidMnemonic(mnemonic)) {


            }
    */
        //generateSeed
        val seed = MnemonicUtils.generateSeed(mnemonic, passPhrase)

        //Create the master keypair
        val masterKeyPair = Bip32ECKeyPair.generateKeyPair(seed)


        //derive the child keypair
        val derivationPath = intArrayOf(
            44 or Bip32ECKeyPair.HARDENED_BIT,
            60 or Bip32ECKeyPair.HARDENED_BIT,
            0 or Bip32ECKeyPair.HARDENED_BIT, 0, 0
        )

        val childKeypair = Bip32ECKeyPair.deriveKeyPair(masterKeyPair, derivationPath)
        val publicKey = childKeypair.publicKey
        val hexOfIt = childKeypair.privateKey.toString(16)
        //create credentials
        val credentials = Credentials.create(childKeypair)

        if (WalletUtils.isValidPrivateKey(hexOfIt) && isValidMnemonic(mnemonic)) {
            return RepointWallet(
                phrase = mnemonic,
                name = walletName,
                creationDate = getCurrentDate(),
                network = ethereumNetwork,
                publicKey = publicKey.toString(),
                privateKey = hexOfIt,
                address = credentials.address,
                balance = 0.0
            )
        } else {
            return null
        }

    }

    fun getCurrentDate(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val createdAt = current.format(formatter)

        return createdAt
    }


    fun mnemonicToBinary(mnemonic: String, wordList: List<String>): String {
        val words = mnemonic.split(" ")
        return words.joinToString("") { word ->
            val index = wordList.indexOf(word)
            String.format("%11s", Integer.toBinaryString(index)).replace(' ', '0')
        }
    }

    fun binaryToEntropy(binary: String): Pair<String, String> {
        val checksumLength = binary.length / 33
        val entropy = binary.substring(0, binary.length - checksumLength)
        val checksum = binary.substring(binary.length - checksumLength)
        return Pair(entropy, checksum)
    }


    fun calculateChecksum(entropy: String): String {
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

    /*fun calculateChecksum(entropy: String): String {
        val entropyBytes = entropy.chunked(8).map { it.toInt(2).toByte() }.toByteArray()
        val hash = MessageDigest.getInstance("SHA-256").digest(entropyBytes)
        val checksumLength = entropy.length / 32
        val checksum = hash[0].toInt() shr (8 - checksumLength) and ((1 shl checksumLength) - 1)
        return String.format("%${checksumLength.toString()}d", Integer.toBinaryString(checksum))
    }*/

    fun isValidMnemonic(mnemonic: String): Boolean {
        val binary = mnemonicToBinary(mnemonic, MnemonicUtils.getWords())
        val (entropy, checksum) = binaryToEntropy(binary)
        val calculatedChecksum = calculateChecksum(entropy)
        return calculatedChecksum == checksum
    }

    /* fun isValidMnemonic(mnemonic: String): Boolean {
        val words = mnemonic.split(" ")
        // Validate word count
        if (words.size !in listOf(12, 15, 18, 21, 24)) {
            return false // Invalid number of words
        }

        return try {
            // Decode the mnemonic into entropy + checksum
            val entropyWithChecksum = MnemonicUtils.generateEntropyFromMnemonic(mnemonic)
            val entropy =
                entropyWithChecksum.copyOf(entropyWithChecksum.size - 1) // Remove checksum bits

            // Calculate the checksum
            val calculatedChecksum = MnemonicUtils.calculateChecksum(entropy)
            val providedChecksum = MnemonicUtils.extractChecksum(entropyWithChecksum)

            // Compare calculated and provided checksum
            calculatedChecksum == providedChecksum
        } catch (e: Exception) {
            false // Any failure means it's invalid
        }
    }*/
}