package com.repoint.models.sharedmodels.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "actives",
    foreignKeys = [
        ForeignKey(
            entity = TokenEntity::class, // Link with BlockchainNetwork
            parentColumns = ["id"], // Foreign Key Parent
            childColumns = ["networkId"], // Foreign Key Child
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LocalActiveNetworks(
    @PrimaryKey val Id: Int = 0, // Stores active network IDs
    val networkId : Int
)
