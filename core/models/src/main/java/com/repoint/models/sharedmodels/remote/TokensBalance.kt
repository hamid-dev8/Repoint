package com.repoint.models.sharedmodels.remote

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TokensBalance(
    @SerializedName("token_address") val tokenAddress: String,
    val symbol: String,
    val name :  String,
    val logo : String,
    val thumbnail : String,
    val decimals : Int,
    val balance : String,
    @SerializedName("possible_spam")val possibleSpam : String = "",
    @SerializedName("verified_contract")val verifiedContract : Boolean = true,
    @SerializedName("total_supply")val totalSupply : String = "",
    @SerializedName("total_supply_formatted")val totalSupplyFormated : String = "",
    @SerializedName("percentage_relative_to_total_supply")val percentageRelativeToTotalSupply : Double = 0.0,
    @SerializedName("security_score") val securityScore : Int = 0,
    @SerializedName("balance_formatted") val balanceFormatted : String = "0",
    @SerializedName("usd_price") val usdPrice : Float = 0.0f,
    @SerializedName("usd_price_24hr_percent_change") val usdPriceDayPercentChange : Float = 0.0f,
    @SerializedName("usd_value") val usdValue : Float = 0.0f,
    @SerializedName("usd_value_24hr_usd_change") val usdValueDayUsdChange : Float = 0.0f,
    @SerializedName("native_token") val nativeToken : Boolean = false,
    @SerializedName("portfolio_percentage") val portfolioPercentage : Float = 0.0f

) : Parcelable
