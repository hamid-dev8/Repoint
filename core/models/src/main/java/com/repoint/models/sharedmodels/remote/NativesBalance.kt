package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class NativesBalance(
    val cursor : String?,
    val page : Int,
    @SerializedName("page_size")val pageSize: Int?,
    val result : List<TokensBalance>?
)
