package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class RepointNFTTransfers(
    @SerializedName("token_address") val tokenAddress: String,
    @SerializedName("token_id")val tokenId : String,
    @SerializedName("from_address_entity")val fromAddressEntity : String,
    @SerializedName("from_address_entity_logo")val fromAddressEntityLogo : String,
    @SerializedName("from_address")val fromAddress : String,
    @SerializedName("from_address_label") val fromAddressLabel : String,
    @SerializedName("to_address_entity") val toAddressEntity : String,
    @SerializedName("to_address_entity_logo")val toAddressEntityLogo : String,
    @SerializedName("to_address")val toAddress : String,
    @SerializedName("to_address_label")val toAddressLabel : String,
    @SerializedName("value")val value : String,
    @SerializedName("amount")val amount : String,
    @SerializedName("contract_type")val contractType : String,
    @SerializedName("block_number")val blockNumber : String,
    @SerializedName("block_timestamp")val blockTimestamp : String,
    @SerializedName("block_hash")val blockHash : String,
    @SerializedName("transaction_hash")val transactionHash : String,
    @SerializedName("transaction_type")val transactionType : String,
    @SerializedName("transaction_index")val transactionIndex : Int,
    @SerializedName("log_index")val logIndex : Int,
    @SerializedName("operator")val operator  :String,
    @SerializedName("possible_spam")val possibleSpam : String,
    @SerializedName("verified_collection")val verifiedCollection : String
)
