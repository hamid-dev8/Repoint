package com.repoint.models.sharedmodels.remote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Platform(val name : String,val coin : PlatformCoin) : Parcelable
