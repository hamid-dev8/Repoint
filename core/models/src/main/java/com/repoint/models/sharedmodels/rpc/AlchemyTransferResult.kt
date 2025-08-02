package com.repoint.models.sharedmodels.rpc

data class AlchemyTransferResult(val transfers : List<AlchemyTransferItem>,val pageKey : String? = null)
