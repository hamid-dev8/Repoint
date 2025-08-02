package com.repoint.network.di

import android.util.Log
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Web3Provider @Inject constructor() {

    private val ALCHEMY_API_KEY = "bAyoxiiQWwUCS2jJdMZ9hVkoKPZwD9dB"


    private val nodes = mapOf(
        1 to "https://eth-mainnet.g.alchemy.com/v2/$ALCHEMY_API_KEY",
        137 to "https://polygon-mainnet.g.alchemy.com/v2/$ALCHEMY_API_KEY",
        56 to "https://bnb-mainnet.g.alchemy.com/v2/$ALCHEMY_API_KEY", // or from another provider
    )

    private val clients = mutableMapOf<Int, Web3j>()

    fun getWeb3j(chainId: Int): Web3j {
        Log.d("Web3Chain", "🔧 Getting Web3j for chainId=$chainId")

        return clients.getOrPut(chainId) {
            val url = nodes[chainId] ?: error("❌ No node URL defined for chainId=$chainId")
            Log.d("Web3Chain", "🌐 Using node URL: $url")

            Log.d("Web3Chain", "✅ Built Web3j for $chainId")

            Web3j.build(HttpService(url))
        }
    }

}