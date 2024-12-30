package com.repoint.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.repoint.models.sharedmodels.RepointWallet
import com.repoint.models.sharedmodels.User


@Dao
interface AuthDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun authWallet(wallet: RepointWallet)

    @Query("SELECT * FROM wallets WHERE walletId = :id LIMIT 1")
    suspend fun getWallet(id : String) : RepointWallet

}