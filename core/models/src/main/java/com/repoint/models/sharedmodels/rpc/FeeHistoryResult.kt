package com.repoint.models.sharedmodels.rpc

data class FeeHistoryResult(
    val baseFeePerGas : List<String>,
    val reward : List<List<String>>
)
