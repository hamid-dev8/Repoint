package com.repoint.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.repoint.models.sharedmodels.local.BlockchainNetworkEntity
import com.repoint.models.sharedmodels.local.LocalActiveNetworks
import com.repoint.models.sharedmodels.remote.BlockchainNetwork


@Dao
interface NetworkDao
{

    @Query("SELECT * From networks")
    fun getAllNetworks() : List<BlockchainNetworkEntity>

    @Query("SELECT * From actives")
    fun getActiveNetworks() : List<LocalActiveNetworks>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNetworks(networks : List<BlockchainNetworkEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActiveNetwork(active : LocalActiveNetworks)

    @Query("DELETE FROM actives WHERE id = :networkId")
    suspend fun deleteActiveNetwork(networkId : Int)
}