package com.repoint.sources.datarepo.datasource

import com.repoint.models.sharedmodels.remote.NativesBalance
import com.repoint.models.sharedmodels.remote.TokenPriceResponse

interface TokenDataSource {

    suspend fun getTokenBalance(address: String, chain: String) : NativesBalance
    suspend fun getTokenPrice(tokenAddress : String,chain: String) : TokenPriceResponse
}