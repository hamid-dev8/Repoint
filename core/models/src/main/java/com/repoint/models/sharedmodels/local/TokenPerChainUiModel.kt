package com.repoint.models.sharedmodels.local

import com.repoint.models.sharedmodels.rpc.AlchemyChain

data class TokenPerChainUiModel(
    val tokenId: Int,
    val name: String,
    val symbol: String,
    val logo: String?,
    val description: String?,
    val contractAddress: String,
    val chainName: AlchemyChain,
    val chainDisplayName: String
)