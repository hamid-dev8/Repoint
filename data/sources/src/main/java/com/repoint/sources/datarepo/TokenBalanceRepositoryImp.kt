package com.repoint.sources.datarepo

import com.repoint.models.sharedmodels.remote.BalanceByWallet
import com.repoint.models.sharedmodels.remote.NativesBalance
import com.repoint.models.sharedmodels.remote.TokenPriceRequestBody
import com.repoint.models.sharedmodels.remote.TokenPriceRequestItem
import com.repoint.models.sharedmodels.remote.TokenPriceResponse
import com.repoint.models.sharedmodels.remote.TokenPriceResponseItem
import com.repoint.network.util.WebApi
import com.repoint.sources.datarepo.datasource.TokenDataSource
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TokenBalanceRepositoryImp @Inject constructor(private val api : WebApi) :
    TokenDataSource
{
    override suspend fun getTokenBalance(address: String, chain: String,tokenAddress: List<String>?) : NativesBalance {
        return api.getTokenBalances(address,chain,tokenAddress)
    }

    override suspend fun getTokenPricesByContract(
        chain: String,
        tokens: List<TokenPriceRequestItem>
    ): List<TokenPriceResponseItem> {
        return api.getTokenPricesByContract(
            chain = chain,
            include = "percent_change",
            requestBody = TokenPriceRequestBody(tokens)
        )    }

    override suspend fun getBalanceByWallet(address: String, chain: String): List<BalanceByWallet> {
        return api.getBalanceByWallet(address,chain)
    }

    override suspend fun getTokenPrice(tokenAddress: String, chain: String): TokenPriceResponse {
        return api.getTokenPrice(tokenAddress,chain)
    }

}