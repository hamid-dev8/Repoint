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
        "ethereum", "polygon", "matic", "arbitrum", "optimism", "avalanche", "avax", "fantom", "ftm" -> 60
        "bnb", "bsc" -> 714
        else -> 60
    }
}

// Shared-address EVM wallet strategy: EVM chains reuse the same base Ethereum address.
val ETHEREUM_COMPATIBLE_CHAINS = setOf(
    "ethereum", "eth", "polygon", "matic", "arbitrum", "optimism",
    "avalanche", "avax", "fantom", "ftm"
)
val BNB_COMPATIBLE_CHAINS = setOf("bnb", "bsc")

fun getWalletCoinType(chainSlug: String): Int {
    val normalized = normalizeSlug(chainSlug)
    return when {
        ETHEREUM_COMPATIBLE_CHAINS.contains(normalized) -> 60
        BNB_COMPATIBLE_CHAINS.contains(normalized) -> 714
        else -> 60
    }
}

fun getAddressForChain(chainSlug: String, chainWallets: List<ChainWallet>): String? {
    val evmAddress = chainWallets.firstOrNull { it.coinType == 60 }?.address

    return when (normalizeSlug(chainSlug)) {
        "polygon", "ethereum", "arbitrum", "optimism", "avalanche", "avax", "fantom", "ftm" -> evmAddress
        "bnb", "bsc" -> chainWallets.firstOrNull { it.coinType == 714 }?.address ?: evmAddress
        else -> evmAddress
    }
}
