package com.repoint.models.sharedmodels.remote

import com.repoint.models.sharedmodels.rpc.AlchemyChainNativeBalance
import java.math.BigDecimal

data class ChainInfo(val moralisChain: String, val wrappedTokenAddress: String)

val chainInfoMap = mapOf(
    1 to "0xC02aaA39b223FE8D0A0E5C4F27eAD9083C756Cc2",     // ETH
    56 to "0xBB4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c",    // BNB
    137 to "0x0d500B1d8E8eF31E21C99d1Db9A6444d3ADf1270",   // MATIC
    43114 to "0xB31f66AA3C1e785363F0875A1B74E27b85FD66c7", // AVAX
    250 to "0x21be370d5312f44cb42ce377bc9b8a0cef1a4c83",   // FTM
    42161 to "0x82af49447d8a07e3bd95bd0d56f35241523fbab1", // Arbitrum
    10 to "0x4200000000000000000000000000000000000006" //Optimism
)

val moralisChainMap: Map<Int, String> = mapOf(
    1 to "eth",
    56 to "bsc",
    137 to "polygon",
    43114 to "avalanche",
    250 to "fantom",
    42161 to "arbitrum",
    10 to "optimism"
)

// Manual override for known native coins
val symbolToChain = mapOf(
    "ETH" to "eth",
    "BNB" to "bsc",
    "MATIC" to "polygon",
    "POL" to "polygon",  // ← Your case
    "AVAX" to "avalanche",
    "FTM" to "fantom",
    "ARB" to "arbitrum",
    "OP" to "optimism"
)

fun getNativeBalanceForMetaSmart(
    meta: TokenMetaData,
    nativeBalances: List<AlchemyChainNativeBalance>
): BigDecimal? {
    val slug = meta.platform?.slug?.lowercase()
    val symbol = meta.symbol.uppercase()

    // First: try to match by curated map
    val chainKey = symbolToChain[symbol]

    // Fallback: try slug
    return nativeBalances.firstOrNull {
        it.chainName.equals(chainKey, true) ||
                it.chainName.equals(slug, true)
    }?.balance
}

