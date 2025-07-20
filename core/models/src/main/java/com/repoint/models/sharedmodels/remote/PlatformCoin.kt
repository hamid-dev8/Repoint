package com.repoint.models.sharedmodels.remote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlatformCoin(val id : String,val name : String,val symbol : String,val slug : String) : Parcelable
