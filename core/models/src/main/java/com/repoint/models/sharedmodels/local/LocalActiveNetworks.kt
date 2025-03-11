package com.repoint.models.sharedmodels.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "actives",
    foreignKeys = [
        ForeignKey(
            entity = BlockchainNetworkEntity::class, // Link with BlockchainNetwork
            parentColumns = ["id"], // Foreign Key Parent
            childColumns = ["id"], // Foreign Key Child
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LocalActiveNetworks(
    @PrimaryKey val id: Int // Stores active network IDs
)
