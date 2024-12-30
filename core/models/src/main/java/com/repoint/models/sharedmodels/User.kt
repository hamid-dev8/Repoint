package com.repoint.models.sharedmodels

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = RepointWallet::class,
            parentColumns = ["walletId"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.CASCADE
        )
    ], indices = [Index(value = ["walletId"])]) // improve query on foreign key
data class User(@PrimaryKey(autoGenerate = true) val userId : Int,val walletId : String,val passwordHash : String = "0",val createdAt : Long)
