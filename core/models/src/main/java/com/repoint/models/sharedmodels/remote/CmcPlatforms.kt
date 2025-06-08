package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class CmcPlatforms(val id : Int,val name : String,val symbol : String,val slug : String,@SerializedName("token_address")val tokenAddress : String)
