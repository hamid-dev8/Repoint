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
import kotlinx.coroutines.flow.Flow


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

    @Query("SELECT * From actives WHERE masterWalletId = :walletId")
    suspend fun getActiveNetworks(walletId: String) : List<LocalActiveNetworks>

    //debug

    @Query("SELECT * FROM actives WHERE masterWalletId =:masterWalletId")
    suspend fun getAllActiveNetworksDebug(masterWalletId: String): List<LocalActiveNetworks>


    /*

 @Query("SELECT * FROM tokens WHERE tokenId IN (SELECT tokenId FROM actives)")
    suspend fun getActiveTokens() : List<TokenEntity>
*/

    @Query("""
    SELECT * FROM tokens
    WHERE masterWalletId = :masterWalletId
    AND tokenId IN (
        SELECT tokenId FROM actives WHERE masterWalletId = :masterWalletId
    )
""") fun getActiveTokens(masterWalletId: String): Flow<List<TokenEntity>>

    @Query("SELECT * FROM tokens WHERE masterWalletId = :walletId AND tokenId IN (SELECT tokenId FROM actives WHERE masterWalletId = :walletId)")
    suspend fun getActiveTokensNow(walletId: String): List<TokenEntity>

    // ✅ Optional: Get all tokens for a specific wallet and network
    @Query("SELECT * FROM tokens WHERE networkId = :networkId AND masterWalletId = :walletId")
    suspend fun getTokensForNetworkInWallet(networkId: Int, walletId: String): List<TokenEntity>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNetworks(networks : List<BlockchainNetworkEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertActiveNetwork(active: LocalActiveNetworks)

    @Query("SELECT * FROM actives")
    suspend fun getAllActiveNetworksDebug(): List<LocalActiveNetworks>

/*    @Query("DELETE FROM actives WHERE  tokenId= :networkId")
    suspend fun deleteActiveNetwork(networkId : Int)*/

 @Query("DELETE FROM actives WHERE tokenId = :tokenId AND masterWalletId = :masterWalletId")
 suspend fun deleteActiveNetwork(tokenId: Int, masterWalletId: String)
}