package com.repoint.network

import retrofit2.http.GET
import retrofit2.http.Header

interface WebApi
{

    //Get ERC-20 token balances for a wallet
    @GET("account/erc20")
    suspend fun getTokenBalances(
        @Header("X-API-key") apiKey
    )

}