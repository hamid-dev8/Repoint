package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class NativePriceResponse(
    val value: String,
    val decimals: Int,
    val name: String,
    val symbol: String,
    @SerializedName("address") val WAddress: String
)