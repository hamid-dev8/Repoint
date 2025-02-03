package com.repoint.models.sharedmodels.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID


@Entity(
    tableName = "wallets",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.SET_NULL // Keeps wallets if a user is deleted
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class RepointWallet(
    @PrimaryKey val walletId: String = UUID.randomUUID().toString(),
    val userId : String?,
    val phrase : String,
    val name: String?,
    val creationDate: String,
    val network: String,
    val publicKey: String,
    val privateKey: String,
    val address : String,
    val balance: Double
)
