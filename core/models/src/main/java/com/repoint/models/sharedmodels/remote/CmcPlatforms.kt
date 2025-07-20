package com.repoint.models.sharedmodels.remote

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CmcPlatforms(val id : Int,val name : String,val symbol : String,val slug : String,@SerializedName("token_address")val tokenAddress : String) : Parcelable
