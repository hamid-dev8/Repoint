package com.repoint.network.util

import com.repoint.models.sharedmodels.remote.NetworkSummary
import com.repoint.models.sharedmodels.rpc.AlchemyNativeBalanceResponse
import com.repoint.models.sharedmodels.rpc.AlchemyTokenBalanceResponse
import com.repoint.models.sharedmodels.rpc.FeeHistoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NetworkApiService
{

/*    @GET("get-contracts")
    suspend fun getNetworks() : List<BlockchainNetwork>*/

    @POST("get-contracts")
    suspend fun getNetworkSummary() : NetworkSummary


    @POST(".")
    suspend fun getTokenBalances(
        @Body body : Map<String, @JvmSuppressWildcards Any>
    ) : AlchemyTokenBalanceResponse

    @POST(".")
    suspend fun getNativeBalance(@Body body: Map<String, @JvmSuppressWildcards Any>): AlchemyNativeBalanceResponse

    @POST(".")
    suspend fun getFeeHistory(@Body body : Map<String,@JvmSuppressWildcards Any>) : FeeHistoryResponse
}