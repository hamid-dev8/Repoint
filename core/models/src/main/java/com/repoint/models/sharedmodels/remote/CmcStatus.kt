package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class CmcStatus(
    val timestamp: String,
    @SerializedName("error_code") val errorCode: String,
    @SerializedName("error_message") val errorMessage: String?,
    val elapsed : Int?,
    @SerializedName("credit_count")val creditCount : Int,
    val notice : String?
){
    companion object {
        val EMPTY = CmcStatus(
            timestamp    = "",
            errorCode    = "0",
            errorMessage = null,
            elapsed      = null,
            creditCount  = 0,
            notice       = null
        )
    }
}
