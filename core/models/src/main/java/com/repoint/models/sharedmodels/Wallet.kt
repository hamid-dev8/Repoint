package com.repoint.models.sharedmodels

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID


@Entity(
    tableName = "wallets",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Wallet(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: Int,
    val name: String?,
    val creationDate: Long,
    val network: String,
    val publicKey: String,
    val privateKey: String,
    val balance: Double
)
