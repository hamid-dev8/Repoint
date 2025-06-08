package com.repoint.network.di

import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Web3Provider @Inject constructor() {

    private val nodes = mapOf(
        //mainnet (when Ready)
        //1 to https,....

        11155111 to "https://eth-sepolia.g.alchemy.com/v2/bAyoxiiQWwUCS2jJdMZ9hVkoKPZwD9dB",
        80002 to "https://polygon-amoy.g.alchemy.com/v2/bAyoxiiQWwUCS2jJdMZ9hVkoKPZwD9dB"
        /*1 to "https://site1.moralis-nodes.com/eth/295e6814f2704d189f72446e9f8dfe10", // eth
        137 to  "https://site1.moralis-nodes.com/polygon/95c7139ee35e410daae09e53d1725518" // pol*/
    )

    private val clients = mutableMapOf<Int, Web3j>()

    fun getWeb3j(chainId: Int): Web3j {
        return clients.getOrPut(chainId) {
            val url = nodes[chainId] ?: error("No node URL defined for chainId=$chainId")
            Web3j.build(HttpService(url))
        }
    }

}