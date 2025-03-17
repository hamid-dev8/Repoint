package com.repoint.models.sharedmodels.local

import androidx.room.Embedded
import androidx.room.Relation

data class BlockchainNetworkWithTokens(
    @Embedded val network: BlockchainNetworkEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "networkId"
    )
    val tokens: List<TokenEntity>
)
