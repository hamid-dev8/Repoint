package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class TokensBalance(
    @SerializedName("token_address") val tokenAddress: String,
    val symbol: String,
    val name :  String,
    val logo : String,
    val thumbnail : String,
    val decimals : Int,
    val balance : String,
    @SerializedName("possible_spam")val possibleSpam : String,
    @SerializedName("verified_contract")val verifiedContract : Boolean,
    @SerializedName("total_supply")val totalSupply : String,
    @SerializedName("total_supply_formatted")val totalSupplyFormated : String,
    @SerializedName("percentage_relative_to_total_supply")val percentageRelativeToTotalSupply : Double,
    @SerializedName("security_score") val securityScore : Int,
    @SerializedName("balance_formatted") val balanceFormatted : String,
    @SerializedName("usd_price") val usdPrice : Float,
    @SerializedName("usd_price_24hr_percent_change") val usdPriceDayPercentChange : Float,
    @SerializedName("usd_value") val usdValue : Float,
    @SerializedName("usd_value_24hr_usd_change") val usdValueDayUsdChange : Float,
    @SerializedName("native_token") val nativeToken : Boolean,
    @SerializedName("portfolio_percentage") val portfolioPercentage : Float

)
