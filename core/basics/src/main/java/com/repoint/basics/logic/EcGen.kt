package com.repoint.basics.logic

import android.util.Log
import com.repoint.models.sharedmodels.RepointWallet
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils

class EcGen() {

    companion object {
        private val entropyManager: EntropyManager = EntropyManager
    }

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

        val date = System.currentTimeMillis()

        val wallet = RepointWallet(
            phrase = phrase,
            name = "wallet",
            creationDate = date,
            network = "Ethereum",
            publicKey = publicKey.toString(),
            privateKey = hexOfIt,
            address = credentials.address,
            balance = 0.0
        )


        Log.d("ETH", "private key Hex = $hexOfIt")
        Log.d("ETH", "credentials = ${credentials.address}")

        return wallet
    }

}