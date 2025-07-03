package com.repoint.network.util

import android.util.Log
import com.repoint.models.sharedmodels.rpc.AlchemyChain
private val platformToChainMap: Map<String, AlchemyChain> = mapOf(
    "ethereum" to AlchemyChain.ETHEREUM,
    "polygon" to AlchemyChain.POLYGON,
    "pol (prev. matic)" to AlchemyChain.POLYGON,
    "matic" to AlchemyChain.POLYGON,
    "bnb smart chain (bep20)" to AlchemyChain.BNB,
    "arbitrum" to AlchemyChain.ARBITRUM,
    "optimism" to AlchemyChain.OPTIMISM,
)

fun resolveChainFromPlatform(platformName: String): AlchemyChain? {

    val normalized = platformName.lowercase()
    val result = when {
        normalized.contains("eth") -> AlchemyChain.ETHEREUM
        normalized.contains("polygon") || normalized.contains("matic") -> AlchemyChain.POLYGON
        normalized.contains("bnb") || normalized.contains("bsc") -> AlchemyChain.BNB
        else -> null
    }
    Log.d("ChainResolver", "Resolved $platformName → $result")
    return result
}
