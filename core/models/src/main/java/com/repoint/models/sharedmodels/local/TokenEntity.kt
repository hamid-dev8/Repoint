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
        ),
        ForeignKey(
            entity = MasterWallet::class, // ✅ Add this!
            parentColumns = ["masterWalletId"],
            childColumns = ["masterWalletId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["networkId"]), Index(value = ["masterWalletId"])] // ✅ Index it for queries
)
data class TokenEntity(
    @PrimaryKey(autoGenerate = true) val tokenId: Int,
    val name: String,
    val symbol: String,
    val contractAddress: String,
    val decimals: Int,
    val logoUrl: String,
    val networkId: Int, // FK to BlockchainNetworkEntity
    // ✅ New fields:
    val masterWalletId: String, // FK to MasterWallet
)