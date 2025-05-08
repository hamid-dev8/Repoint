package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class TokenPriceResponse(
    val tokenName: String,
    val tokenSymbol: String,
    val tokenLogo: String,
    val tokenDecimals: String,
    val nativePrice: NativePriceResponse,
    val usdPrice : Float,
    val usdPriceFormatted : String,
    val exchangeName : String,
    val exchangeAddress : String,
    @SerializedName("tokenAddress")val WTokenAddress : String,
    val priceLastChangedAtBlock : String,
    val blockTimeStamp : String,
    val possibleSpam : Boolean,
    val verifiedContract : Boolean,
    val pairAddress : String,
    val pairTotalLiquidityUsd : String,
    val securityScore : Int,
    val usdPrice24hr : Float,
    val usdPrice24hrUsdChange : Float,
    val usdPrice24hrPercentChange : Float,
    @SerializedName("24hrPercentChange")val usdPercentChange : String
)