package com.repoint.models.sharedmodels.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "actives",
    foreignKeys = [
        ForeignKey(
            entity = TokenEntity::class, // Link with BlockchainNetwork
            parentColumns = ["tokenId"], // Foreign Key Parent
            childColumns = ["tokenId"], // Foreign Key Child
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tokenId"])]
)
data class LocalActiveNetworks(
    @PrimaryKey val tokenId: Int // Stores active network IDs
)
