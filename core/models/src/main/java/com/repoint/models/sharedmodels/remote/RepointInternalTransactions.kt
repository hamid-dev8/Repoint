package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class RepointInternalTransactions(
    @SerializedName("transaction_hash") val transactionHash: String,
    @SerializedName("block_number")val blockNumber : String,
    @SerializedName("block_hash")val blockHash : String,
    @SerializedName("type")val type : String,
    @SerializedName("from")val from : String,
    @SerializedName("to")val to : String,
    @SerializedName("value")val value : String,
    @SerializedName("gas")val gas : String,
    @SerializedName("gas_used")val gasUsed : String,
    @SerializedName("input")val input : String,
    @SerializedName("output")val output : String
)
