package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class History(val cursor : String,@SerializedName("page_size")val pageSize : Int,val limit : String,@SerializedName("result")val transactions : List<RepointTransactions>)
