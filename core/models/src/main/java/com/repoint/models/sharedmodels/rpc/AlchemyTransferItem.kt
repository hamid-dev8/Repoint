package com.repoint.models.sharedmodels.rpc

import com.repoint.models.sharedmodels.remote.TokenMetaData

data class AlchemyTransferItem(
    val hash: String,
    val from: String,
    val to : String,
    val value : String?,
    val asset : String?,
    val category: String?,
    val blockNum: String?,
    val rawContract: AlchemyRawContract?, // already parsed from JSON
    val metaData: AlchemyTransferMetadata?
)
