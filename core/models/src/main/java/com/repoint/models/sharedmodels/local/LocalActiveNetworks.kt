package com.repoint.models.sharedmodels.local

import androidx.room.Entity

@Entity(
    tableName = "actives",
    primaryKeys = ["tokenId", "masterWalletId","chain"]
)
data class LocalActiveNetworks(
    val tokenId: Int,
    val tokenAddress : String,
    val masterWalletId: String,
    val chain : String
)

