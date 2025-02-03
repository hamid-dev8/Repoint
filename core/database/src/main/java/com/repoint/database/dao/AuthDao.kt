package com.repoint.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.repoint.models.sharedmodels.local.RepointWallet


@Dao
interface AuthDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun authWallet(wallet: RepointWallet)

    @Query("SELECT * FROM wallets WHERE userId = :userId")
    suspend fun getAllWallets(userId: String) : List<RepointWallet>

    @Query("SELECT * FROM wallets WHERE walletId = :id LIMIT 1")
    suspend fun getWallet(id : String) : RepointWallet

    @Query("UPDATE wallets SET userId = :userId WHERE walletId = :walletId")
    suspend fun linkWalletToUser(walletId : String, userId : String)

}