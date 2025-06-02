package com.repoint.models.sharedmodels.remote

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NativesBalance(
    val cursor : String? = "",
    val page : Int = 1,
    @SerializedName("page_size")val pageSize: Int? = 1,
    val result : List<TokensBalance>
) : Parcelable
