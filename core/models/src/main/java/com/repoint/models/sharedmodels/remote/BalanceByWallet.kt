package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class BalanceByWallet(
    @SerializedName("token_address") val tokenAddress: String,
    val symbol: String,
    val name :  String,
    val logo : String,
    val thumbnail : String,
    val decimals : Int,
    val balance : String,
    @SerializedName("possible_spam")val possibleSpam : Boolean = false,
    @SerializedName("verified_contract")val verifiedContract : Boolean = true,
    @SerializedName("total_supply")val totalSupply : String = "",
    @SerializedName("total_supply_formatted")val totalSupplyFormated : String = "",
    @SerializedName("percentage_relative_to_total_supply")val percentageRelativeToTotalSupply : Double = 0.0,
    @SerializedName("security_score") val securityScore : Int = 0,
    )