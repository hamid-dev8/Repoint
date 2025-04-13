package com.repoint.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.repoint.models.sharedmodels.local.BlockchainNetworkEntity
import com.repoint.models.sharedmodels.local.BlockchainNetworkWithTokens
import com.repoint.models.sharedmodels.local.LocalActiveNetworks
import com.repoint.models.sharedmodels.local.TokenEntity
import com.repoint.models.sharedmodels.local.TokenWithNetwork
import com.repoint.models.sharedmodels.remote.BlockchainNetwork


@Dao
interface NetworkDao
{

   @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTokens(tokens : List<TokenEntity>)

    @Query("SELECT * FROM tokens WHERE  networkId= :networkId")
    suspend fun getTokensForNetwork(networkId: Int): List<TokenEntity>


    /* get embeded token cointype!*/
    @Transaction
    @Query("SELECT * FROM tokens WHERE tokenId = :tokenId")
    suspend fun getTokenWithNetwork(tokenId : Int) : TokenWithNetwork

    @Transaction
    @Query("SELECT * FROM networks")
    suspend fun getNetworksWithTokens(): List<BlockchainNetworkWithTokens>

    @Query("SELECT COUNT(*) FROM networks")
    suspend fun getNetworkCount(): Int

    @Query("SELECT * From networks")
    suspend fun getAllNetworks() : List<BlockchainNetworkEntity>

    @Query("SELECT * FROM networks WHERE id = :networkId LIMIT 1")
    suspend fun getNetworkById(networkId: Int): BlockchainNetworkEntity

    @Query("SELECT * From actives")
    suspend fun getActiveNetworks() : List<LocalActiveNetworks>

 @Query("SELECT * FROM tokens WHERE tokenId IN (SELECT tokenId FROM actives)")
    suspend fun getActiveTokens() : List<TokenEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNetworks(networks : List<BlockchainNetworkEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActiveNetwork(active : LocalActiveNetworks)

    @Query("DELETE FROM actives WHERE  tokenId= :networkId")
    suspend fun deleteActiveNetwork(networkId : Int)
}