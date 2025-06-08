package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class ContractAddress(@SerializedName("contract_address")val contractAddress: String,val platform : Platform)
