package com.repoint.models.sharedmodels.rpc

data class AlchemyTokenBalancesResult(val address : String , val tokenBalances: List<AlchemyTokenBalance>)
