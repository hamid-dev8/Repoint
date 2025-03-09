package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class RepointNativeTransfers(
    @SerializedName("from_address_entity") val fromAddressEntity: String,
    @SerializedName("from_address_entity_logo")val fromAddressEntityLogo : String,
    @SerializedName("from_address")val fromAddress : String,
    @SerializedName("from_address_label")val fromAddressLabel : String,
    @SerializedName("to_address_entity")val toAddressEntity : String,
    @SerializedName("to_address_entity_logo")val toAddressEntityLogo : String,
    @SerializedName("to_address")val toAddress : String,
    @SerializedName("to_address_label")val toAddressLabel : String,
    @SerializedName("value")val value : String,
    @SerializedName("value_formatted")val valueFormatted : String,
    @SerializedName("direction")val direction : String,
    @SerializedName("internal_transaction") val internalTransaction : String,
    @SerializedName("token_symbol")val tokenSymbol : String,
    @SerializedName("token_logo")val tokenLogo : String
)
