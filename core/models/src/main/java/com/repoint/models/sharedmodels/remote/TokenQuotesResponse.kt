package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class TokenQuotesResponse(val status: CmcStatus,val data : Map<String,QuoteData>)

data class QuoteData(
    val id : Int,
    val name : String,
    val symbol : String,
    val slug : String,
    val quote : Map<String,QuoteCurrency>
)

data class QuoteCurrency(
    @SerializedName("price") val price : Double,
    @SerializedName("volume_24h") val volume24h : Double,
    @SerializedName("percent_change_1h") val change1h : Double,
    @SerializedName("percent_change_24h") val change24h : Double,
    @SerializedName("percent_change_7d") val change7d : Double
)