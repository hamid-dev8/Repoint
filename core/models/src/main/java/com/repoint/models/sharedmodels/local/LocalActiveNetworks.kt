package com.repoint.models.sharedmodels.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "actives",
    primaryKeys = ["tokenId", "masterWalletId"]
)
data class LocalActiveNetworks(
    val tokenId: Int,
    val masterWalletId: String
)

