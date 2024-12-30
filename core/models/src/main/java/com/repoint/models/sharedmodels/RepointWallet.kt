package com.repoint.models.sharedmodels

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID


@Entity(tableName = "wallets")
data class RepointWallet(
    @PrimaryKey val walletId: String = UUID.randomUUID().toString(),
    val phrase : String,
    val name: String?,
    val creationDate: Long,
    val network: String,
    val publicKey: String,
    val privateKey: String,
    val address : String,
    val balance: Double
)
