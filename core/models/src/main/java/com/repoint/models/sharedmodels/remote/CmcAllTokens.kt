package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class CmcAllTokens(
    val id: Int,
    val rank: Int,
    val name: String,
    val symbol: String,
    val slug: String,
    @SerializedName("is_active") val isActive: Int,
    val status: Int,
    @SerializedName("first_historical_data")val  firstHistoricalData: String,
    @SerializedName("last_historical_data")val lastHistoricalData : String,
    val platform : CmcPlatforms?
)
