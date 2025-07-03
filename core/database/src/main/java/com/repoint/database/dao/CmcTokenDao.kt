package com.repoint.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.repoint.models.sharedmodels.local.ActiveTokenKey
import com.repoint.models.sharedmodels.local.CmcTokenEntity
import com.repoint.models.sharedmodels.local.LocalActiveNetworks
import kotlinx.coroutines.flow.Flow

@Dao
interface CmcTokenDao
{
    @Query("SELECT * FROM cmc_tokens ORDER BY rank ASC")
    fun getAllTokens() : List<CmcTokenEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTokens(tokens : List<CmcTokenEntity>)

    @Query("DELETE FROM cmc_tokens")
    suspend fun clearAll()

    @Query("""
    SELECT * FROM cmc_tokens 
    WHERE name LIKE '%' || :query || '%' 
       OR symbol LIKE '%' || :query || '%' 
       OR slug LIKE '%' || :query || '%'
    ORDER BY rank ASC
""")
    suspend fun searchTokensByQuery(query: String): List<CmcTokenEntity>

    @Query("UPDATE cmc_tokens SET logo = :logo, description = :description, websiteUrl = :websiteUrl, lastUpdated = :lastUpdated WHERE id = :id")
    suspend fun updateMetadata(id: Int, logo: String?, description: String?, websiteUrl: String?, lastUpdated: Long)

    //the active tokens where you set your ids
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActiveToken(active : LocalActiveNetworks)

    @Query("SELECT tokenId FROM actives WHERE masterWalletId = :walletId")
    fun getActiveTokenIds(walletId: String) : Flow<List<Int>>

    @Query("DELETE FROM actives WHERE tokenId = :tokenId AND masterWalletId = :walletId AND chain = :chain")
    suspend fun deleteActiveNetworks(tokenId : Int,walletId: String,chain : String)

    //count the items
    @Query("SELECT COUNT(*) FROM cmc_tokens")
    suspend fun countTokens() : Int

    @Query("SELECT MAX(lastUpdated) FROM cmc_tokens")
    suspend fun getLastUpdatedTime() : Long?

    @Query("SELECT * FROM cmc_tokens ORDER BY rank ASC LIMIT :limit OFFSET :offset")
    suspend fun getTokensPaged(offset: Int, limit: Int): List<CmcTokenEntity>

    @Query("SELECT tokenId FROM actives WHERE masterWalletId = :walletId")
    suspend fun getActiveTokenContracts(walletId: String): List<Int>

    @Query("SELECT * FROM actives WHERE masterWalletId = :walletId")
    fun getActiveTokenEntities(walletId: String): Flow<List<LocalActiveNetworks>>

    @Query("SELECT tokenId, chain FROM actives WHERE masterWalletId = :walletId")
    fun getActiveTokenKeys(walletId: String): Flow<List<ActiveTokenKey>>

    @Query("SELECT * FROM actives")
    suspend fun debugAllActives(): List<LocalActiveNetworks>

}