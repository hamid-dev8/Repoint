package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class RepointTransactions(
    val hash: String,
    val nonce : String,
    @SerializedName("transaction_index")val transactionIndex : String,
    @SerializedName("from_address_entity")val fromAddressEntity : String,
    @SerializedName("from_address_entity_logo") val fromAddressEntityLogo : String,
    @SerializedName("from_address")val fromAddress : String,
    @SerializedName("from_address_label")val fromAddressLabel : String,
    @SerializedName("to_address_entity")val toAddressEntity : String,
    @SerializedName("to_address_entity_logo")val toAddressEntityLogo : String,
    @SerializedName("to_address")val toAddress : String,
    @SerializedName("to_address_label")val toAddressLabel : String,
    @SerializedName("value")val value : String,
    @SerializedName("gas")val gas : String,
    @SerializedName("gas_price")val gasPrice : String,
    @SerializedName("receipt_cumulative_gas_used") val receiptCumulativeGasUsed : String,
    @SerializedName("receipt_gas_used")val receiptGasUsed : String,
    @SerializedName("receipt_contract_address")val receiptContractAddress : String,
    @SerializedName("receipt_root")val receiptRoot : String,
    @SerializedName("receipt_status") val receiptStatus : String,
    @SerializedName("block_timestamp")val blockTimeStamp : String,
    @SerializedName("block_number") val blockNumber : String,
    @SerializedName("block_hash") val blockHash : String,
    @SerializedName("internal_transactions")val internalTransactions : List<RepointInternalTransactions>,
    @SerializedName("nft_transfers")val nftTransfers : List<RepointNFTTransfers>,
    @SerializedName("erc20_transfer") val erc20Transfers : List<RepointERC20Transfers>,
    @SerializedName("native_transfers") val nativeTransfers : List<RepointNativeTransfers>,
    @SerializedName("category")val category : String,
    @SerializedName("summary") val summary : String
)
