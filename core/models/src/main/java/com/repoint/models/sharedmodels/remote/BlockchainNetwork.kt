package com.repoint.models.sharedmodels.remote

data class BlockchainNetwork(
    val id: Int,
    val name: String,
    val chainId: Int,
    val rpcUrl: String,
    val explorerUrl: String,
    val nativeToken: String,
    val dexRouter: String,
    val tokens : List<Token>,
    val coinType : Int
)
