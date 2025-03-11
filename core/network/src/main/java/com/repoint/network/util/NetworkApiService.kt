package com.repoint.network.util

import com.repoint.models.sharedmodels.remote.NetworkSummary
import retrofit2.http.POST

interface NetworkApiService
{

/*    @GET("get-contracts")
    suspend fun getNetworks() : List<BlockchainNetwork>*/

    @POST("get-contracts")
    suspend fun getNetworkSummary() : NetworkSummary

}