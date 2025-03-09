package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class RepointERC20Transfers(
    @SerializedName("token_name") val tokenName: String,
    @SerializedName("token_symbol")val tokenSymbol : String,
    @SerializedName("token_logo")val tokenLogo : String,
    @SerializedName("token_decimals")val tokenDecimals : String,
    @SerializedName("transaction_hash")val transactionHash : String,
    @SerializedName("address")val address : String,
    @SerializedName("block_timestamp")val blockTimestamp : String,
    @SerializedName("block_number")val blockNumber : String,
    @SerializedName("block_hash")val blockHash : String,
    @SerializedName("to_address_entity")val toAddressEntity : String,
    @SerializedName("to_address_entity_logo")val toAddressEntityLogo : String,
    @SerializedName("to_address")val toAddress : String,
    @SerializedName("to_address_label")val toAddressLabel : String,
    @SerializedName("from_address_entity")val fromAddressEntity : String,
    @SerializedName("from_address_entity_logo")val fromAddressEntityLogo : String,
    @SerializedName("from_address")val fromAddress : String,
    @SerializedName("from_address_label")val fromAddressLabel : String,
    @SerializedName("value")val value : String,
    @SerializedName("transaction_index") val transactionIndex : Int,
    @SerializedName("log_index")val logIndex : Int,
    @SerializedName("possible_spam")val possibleSpam : String,
    @SerializedName("verified_contract")val verifiedContract : String
)
