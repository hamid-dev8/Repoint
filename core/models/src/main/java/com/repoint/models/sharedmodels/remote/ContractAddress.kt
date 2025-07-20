package com.repoint.models.sharedmodels.remote

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
 data class ContractAddress(@SerializedName("contract_address")val contractAddress: String,val platform : Platform) : Parcelable
