package com.repoint.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.repoint.models.sharedmodels.local.CmcTokenEntity

@Dao
interface CmcTokenDao
{
    @Query("SELECT * FROM cmc_tokens ORDER BY rank ASC")
    fun getAllTokens() : List<CmcTokenEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTokens(tokens : List<CmcTokenEntity>)

    @Query("DELETE FROM cmc_tokens")
    suspend fun clearAll()

    @Query("SELECT MAX(lastUpdated) FROM cmc_tokens")
    suspend fun getLastUpdateTime() : Long?

}