package com.repoint.basics.logic

import com.repoint.models.sharedmodels.local.ChainWallet
import com.repoint.models.sharedmodels.remote.ContractAddress
import com.repoint.models.sharedmodels.remote.moralisChainMap
import com.repoint.models.sharedmodels.remote.normalizeSlug
import com.repoint.models.sharedmodels.rpc.AlchemyChain

fun mapPlatformToAlchemyChain(contract: ContractAddress): AlchemyChain? {
    val platform = contract.platform ?: return null
    val platformSlug = platform.coin?.slug
    val platformName = platform.name
    return AlchemyChain.entries.find { chain ->
        chain.name.equals(platformSlug, ignoreCase = true) ||
                chain.name.equals(platformName, ignoreCase = true) ||
                chain.name.equals(platform.coin?.name, ignoreCase = true)
    }
}
val slugToChainIdMap: Map<String, Int> = mapOf(
    "eth" to 1,
    "ethereum" to 1,
    "bnb" to 56,
    "binance-smart-chain" to 56,
    "polygon" to 137,
    "matic" to 137,
    "avalanche" to 43114,
    "avax" to 43114,
    "fantom" to 250,
    "ftm" to 250,
    "arbitrum" to 42161,
    "optimism" to 10
)
fun chainIdFromSlug(slug: String): Int? {
    return slugToChainIdMap[slug.lowercase()]
}

// In your constants or utility file
fun coinTypeFromSlug(slug: String): Int {
    return when (normalizeSlug(slug)) {
        "ethereum" -> 60
        "polygon", "matic" -> 60  // Use Ethereum's coin type for Polygon
        "bnb", "bsc" -> 60        // Keep BNB separate if you want
        "arbitrum" -> 60          // Arbitrum uses same address as Ethereum
        "optimism" -> 60          // Optimism uses same address as Ethereum
        "avalanche" -> 60         // If you want separate addresses for other chains
        "fantom" -> 60
        else -> 60 // Default fallback
    }
}

// Or more explicitly, define which chains share Ethereum addresses
val ETHEREUM_COMPATIBLE_CHAINS = setOf("ethereum", "polygon", "arbitrum", "optimism")
val BNB_COMPATIBLE_CHAINS = setOf("bnb", "bsc")

fun getWalletCoinType(chainSlug: String): Int {
    val normalized = normalizeSlug(chainSlug)
    return when {
        ETHEREUM_COMPATIBLE_CHAINS.contains(normalized) -> 60  // Ethereum
        BNB_COMPATIBLE_CHAINS.contains(normalized) -> 60      // Or separate if needed
        else -> 60
    }
}

fun getAddressForChain(chainSlug: String, chainWallets: List<ChainWallet>): String? {
    val ethAddress = chainWallets.firstOrNull { it.coinType == 60 }?.address

    return when (normalizeSlug(chainSlug)) {
        "polygon", "ethereum", "arbitrum", "optimism" -> ethAddress
        "bnb", "bsc" -> chainWallets.firstOrNull { it.coinType == 56 }?.address ?: ethAddress
        else -> ethAddress // Default to Ethereum address
    }
}
