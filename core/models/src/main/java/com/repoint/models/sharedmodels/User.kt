package com.repoint.models.sharedmodels

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID


@Entity(tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = RepointWallet::class,
            parentColumns = ["walletId"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.CASCADE
        )
    ], indices = [Index(value = ["walletId"])]) // improve query on foreign key
data class User(@PrimaryKey val userId: String = UUID.randomUUID().toString(), val walletId : String, val salt : String, val passwordHash : String = "0", val createdAt : String)
