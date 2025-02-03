package com.repoint.network.util

import com.repoint.models.sharedmodels.remote.NativesBalance
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WebApi
{

    //Get ERC-20 token balances for a wallet
    @GET("wallets/{address}/tokens")
    suspend fun getTokenBalances(
        @Path("address") walletAddress : String,
        @Query("chain") chain : String
    ) : NativesBalance

}