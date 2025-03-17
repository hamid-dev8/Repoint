package com.repoint.sources.datarepo

import com.repoint.models.sharedmodels.remote.NativesBalance
import com.repoint.network.util.WebApi
import com.repoint.sources.datarepo.datasource.TokenDataSource
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TokenBalanceRepositoryImp @Inject constructor(private val api : WebApi) :
    TokenDataSource
{
    override suspend fun getTokenBalance(address: String, chain: String) : NativesBalance {
        return api.getTokenBalances(address,chain)
    }

}