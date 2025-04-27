package com.repoint.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.repoint.models.sharedmodels.local.ChainWallet
import com.repoint.models.sharedmodels.local.MasterWallet
import com.repoint.models.sharedmodels.local.RepointWallet


@Dao
interface AuthDao {

    //Insert Master Wallet
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMasterWallet(masterWallet: MasterWallet)

    //Insert Chain Wallet( s )
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChainWallets(chainWallets : List<ChainWallet>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChainWallet(chainWallets: ChainWallet)

    //get all chainWallets for a Master Wallet
    @Query("SELECT * FROM chain_wallets WHERE masterWalletId = :masterWalletId")
    suspend fun getChainWalletsByMaster(masterWalletId : String) : List<ChainWallet>

    //rename chain wallet
    @Query("UPDATE chain_wallets SET networkName = :newName WHERE chainWalletId = :chainWalletId")
    suspend fun renameChainWallet(chainWalletId : String,newName : String)

    @Query("DELETE FROM chain_wallets WHERE chainWalletId= :chainWalletId")
    suspend fun deleteChainWallet(chainWalletId : String)

    //Get specific chain wallet by masterWalletId and coinType
    @Query("SELECT * FROM chain_wallets WHERE masterWalletId = :masterWalletId AND coinType = :coinType LIMIT 1")
    suspend fun getChainWallet(masterWalletId : String , coinType : Int) : ChainWallet?


    //get master wallet by id
    @Query("SELECT * FROM master_wallets WHERE masterWalletId = :masterWalletId")
    suspend fun getMasterWallet(masterWalletId : String) : MasterWallet

    //Link master wallet to user
    @Query("UPDATE master_wallets SET userId = :userId WHERE masterWalletId = :masterWalletId")
    suspend fun linkMasterWalletToUser(masterWalletId : String,userId : String)

    //get all master Wallets of a user
    @Query("SELECT * FROM master_wallets WHERE userId = :userId")
    suspend fun getAllMasterWallets(userId : String?) : List<MasterWallet>
    /////////////////////////////////////////////////////////////



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun authWallet(wallet: RepointWallet)

    @Query("SELECT * FROM wallets WHERE userId = :userId")
    suspend fun getAllWallets(userId: String) : List<RepointWallet>

    @Query("SELECT * FROM wallets WHERE walletId = :id LIMIT 1")
    suspend fun getWallet(id : String) : RepointWallet

    @Query("UPDATE wallets SET userId = :userId WHERE walletId = :walletId")
    suspend fun linkWalletToUser(walletId : String, userId : String)

}