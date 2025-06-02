package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class TokenPriceRequestItem(
    @SerializedName("token_address") val tokenAddress: String,
    val exchange: String? = null,
    @SerializedName("to_block") val toBlock: String? = null
)


data class TokenPriceRequestBody(
    val tokens: List<TokenPriceRequestItem>
)