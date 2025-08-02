package com.repoint.models.sharedmodels.rpc

data class TxHistoryItem(
    val blockNum : String,
    val hash : String,
    val from : String,
    val to : String,
    val value : String,
    val asset : String,
    val timestamp : String,
    val isIncoming : Boolean,
    val chainSlug : String,
    val category : String,
    val rawContract : RawContracts?
)
