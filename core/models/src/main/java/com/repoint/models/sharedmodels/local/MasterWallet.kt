package com.repoint.models.sharedmodels.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "master_wallets",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class MasterWallet(
    @PrimaryKey val masterWalletId: String = UUID.randomUUID().toString(),
    val userId : String?,
    val phrase : String,
    val name : String,
    val creationDate : String
)
