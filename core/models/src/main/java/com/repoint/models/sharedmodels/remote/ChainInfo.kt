package com.repoint.models.sharedmodels.remote

data class ChainInfo(val moralisChain : String, val wrappedTokenAddress : String)

val chainInfoMap = mapOf(
    1L to "0xC02aaA39b223FE8D0A0E5C4F27eAD9083C756Cc2",     // ETH
    56L to "0xBB4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c",    // BNB
    137L to "0x0d500B1d8E8eF31E21C99d1Db9A6444d3ADf1270",   // MATIC
    43114L to "0xB31f66AA3C1e785363F0875A1B74E27b85FD66c7", // AVAX
    250L to "0x21be370d5312f44cb42ce377bc9b8a0cef1a4c83",   // FTM
    42161L to "0x82af49447d8a07e3bd95bd0d56f35241523fbab1", // Arbitrum
    10L to "0x4200000000000000000000000000000000000006" //Optimism
)

val moralisChainMap = mapOf(
    1L to "eth",
    56L to "bsc",
    137L to "polygon",
    43114L to "avalanche",
    250L to "fantom",
    42161L to "arbitrum",
    10L to "optimism"
)