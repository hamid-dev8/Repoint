package com.repoint.models.sharedmodels.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "chain_wallets",
    foreignKeys = [
        ForeignKey(
            entity = MasterWallet::class,
            parentColumns = ["masterWalletId"],
            childColumns = ["masterWalletId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["masterWalletId"])]
)
data class ChainWallet(
    @PrimaryKey val chainWalletId: String = UUID.randomUUID().toString(),
    val masterWalletId : String,
    val coinType : Int, //BIP44 Coin Type (e.g , 60 , 714)
    val networkName : String,
    val publicKey : String,
    val privateKey : String,
    val address : String,
    val balance : Double = 0.0
)
