package com.repoint.models.sharedmodels.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "cmc_tokens")
data class CmcTokenEntity(
    @PrimaryKey val id : Int,
    val rank : Int,
    val name : String,
    val symbol : String,
    val slug : String,
    val isActive : Int,
    val firstHistoricalData : String?,
    val lastHistoricalData : String?,
    val platformId : Int?,
    val platformName : String?,
    val platformSymbol : String?,
    val platformSlug : String?,
    val tokenAddress : String?,
    val logo : String?,
    val description : String?,
    val websiteUrl : String?,
    val lastUpdated : Long // for sync timestamp
)
