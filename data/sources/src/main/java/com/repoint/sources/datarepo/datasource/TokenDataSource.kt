package com.repoint.sources.datarepo.datasource

import com.repoint.models.sharedmodels.remote.BalanceByWallet
import com.repoint.models.sharedmodels.remote.CmcMapData
import com.repoint.models.sharedmodels.remote.NativesBalance
import com.repoint.models.sharedmodels.remote.TokenPriceRequestItem
import com.repoint.models.sharedmodels.remote.TokenPriceResponse
import com.repoint.models.sharedmodels.remote.TokenPriceResponseItem

interface TokenDataSource {

    suspend fun getTokenBalance(
        address: String,
        chain: String,
        tokenAddress: List<String>?
    ): NativesBalance

    suspend fun getTokenPricesByContract(
        chain: String,
        tokens: List<TokenPriceRequestItem>
    ): List<TokenPriceResponseItem>

    suspend fun getBalanceByWallet(address: String, chain: String): List<BalanceByWallet>
    suspend fun getTokenPrice(tokenAddress: String, chain: String): TokenPriceResponse
}