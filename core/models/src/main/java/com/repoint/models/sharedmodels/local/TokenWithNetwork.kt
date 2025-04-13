package com.repoint.models.sharedmodels.local

import androidx.room.Embedded
import androidx.room.Relation

data class TokenWithNetwork(
    @Embedded val token: TokenEntity,
    @Relation(
        parentColumn = "networkId",
        entityColumn = "id"
    )
    val network : BlockchainNetworkEntity
)
