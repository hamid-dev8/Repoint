package com.repoint.models.sharedmodels.local

import com.repoint.models.sharedmodels.remote.TokenMetaData

data class ResolvedTokenInstance(
    val tokenId: Int,
    val symbol: String,
    val name: String,
    val logo: String?,
    val contractAddress: String,
    val chain: String,
    val decimals: Int,
    val tokenMeta: TokenMetaData
)