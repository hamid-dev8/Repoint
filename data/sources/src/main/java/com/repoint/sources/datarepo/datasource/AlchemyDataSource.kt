package com.repoint.sources.datarepo.datasource

import com.repoint.models.sharedmodels.local.LocalActiveNetworks
import com.repoint.models.sharedmodels.remote.TokenMetaData
import com.repoint.models.sharedmodels.rpc.AlchemyChain
import com.repoint.models.sharedmodels.rpc.AlchemyTokenBalance
import com.repoint.models.sharedmodels.rpc.AlchemyTokenBalanceResponse
import com.repoint.models.sharedmodels.ui.ApiResult
import com.repoint.network.util.NetworkApiService

interface AlchemyDataSource
{
    suspend fun getTokenBalances(walletAddress : String,contracts : List<String>)  : ApiResult<AlchemyTokenBalanceResponse>
    suspend fun getMultiChainTokenBalances(walletAddress: String,tokenMetaMap : Map<String,TokenMetaData>, alchemyClientFactory : (AlchemyChain) -> NetworkApiService) : ApiResult<List<AlchemyTokenBalance>>
    suspend fun getNativeBalance(walletAddress: String): ApiResult<String>

    //db
    suspend fun getTokenContracts(walletAddress: String) : List<Int>
    suspend fun debugAllActives() : List<LocalActiveNetworks>
}