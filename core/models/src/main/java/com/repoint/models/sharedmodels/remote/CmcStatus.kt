package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class CmcStatus(
    val timestamp: String,
    @SerializedName("error_code") val errorCode: String,
    @SerializedName("error_message") val errorMessage: String?,
    val elapsed : Int?,
    @SerializedName("credit_count")val creditCount : Int,
    val notice : String?
)
