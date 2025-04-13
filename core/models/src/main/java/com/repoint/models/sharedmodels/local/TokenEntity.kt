package com.repoint.models.sharedmodels.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tokens",
    foreignKeys = [
        ForeignKey(
            entity = BlockchainNetworkEntity::class,
            parentColumns = ["id"],
            childColumns = ["networkId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["networkId"])]
)
data class TokenEntity(
    @PrimaryKey val tokenId: Int,
    val name: String,
    val symbol: String,
    val contractAddress: String,
    val decimals: Int,
    val logoUrl: String,
    val networkId : Int // Foreign key referencing BlockchainNetworkEntity
)
