package com.repoint.network.util

import com.repoint.models.sharedmodels.remote.BalanceByWallet
import com.repoint.models.sharedmodels.remote.CmcMapData
import com.repoint.models.sharedmodels.remote.History
import com.repoint.models.sharedmodels.remote.NativesBalance
import com.repoint.models.sharedmodels.remote.TokenInfoMetadataResponse
import com.repoint.models.sharedmodels.remote.TokenPriceRequestBody
import com.repoint.models.sharedmodels.remote.TokenPriceResponse
import com.repoint.models.sharedmodels.remote.TokenPriceResponseItem
import com.repoint.models.sharedmodels.remote.TokenQuotesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WebApi {

    //Get the list of all available Tokens
    @GET("v1/cryptocurrency/map")
    suspend fun getTokensListByLimit(
        @Query("start") start : Int,
        @Query("limit") limit : Int
    ) : CmcMapData

    @GET("v1/cryptocurrency/map")
    suspend fun getAllTokensSorted(
        @Query("sort") sort : String = "cmc_rank"
    ) : CmcMapData

    @GET("v1/cryptocurrency/map")
    suspend fun getTokensBySymbol(
        @Query("symbol") symbol: String
    ) : CmcMapData

    @GET("v1/cryptocurrency/map")
    suspend fun searchTokenBySymbolOrSlug(
        @Query("symbol") symbol: String? = null,
    ): CmcMapData


    @GET("v2/cryptocurrency/info")
    suspend fun getTokensInfo(
        @Query("id") ids : String
    ) : TokenInfoMetadataResponse

    @GET("v2/cryptocurrency/info")
    suspend fun getTokensInfoBySlug(
        @Query("slug")slug : String
    ) : TokenInfoMetadataResponse


    @GET("v1/cryptocurrency/quotes/latest")
    suspend fun getTokenPrices(
        @Query("id") ids : String,
        @Query("convert") convert : String = "USD" // any supported fiat / crypto
    ) : TokenQuotesResponse


    //Get ERC-20 token balances for a wallet
    @GET("wallets/{address}/tokens")
    suspend fun getTokenBalances(
        @Path("address") walletAddress: String,
        @Query("chain") chain: String,
        @Query("token_addresses[]") tokenAddress: List<String>? = null
    ): NativesBalance

    @POST("erc20/prices")
    suspend fun getTokenPricesByContract(
        @Query("chain") chain: String,
        @Query("include") include: String = "percent_change",
        @Body requestBody: TokenPriceRequestBody
    ): List<TokenPriceResponseItem>


    @GET("{address}/erc20")
    suspend fun getBalanceByWallet(
        @Path("address") walletAddress: String,
        @Query("chain") chain: String
    ): List<BalanceByWallet>

    @GET("wallets/{address}/history")
    suspend fun getNativeHistory(
        @Path("address") walletAddress: String,
        @Query("chain") chain: String,
        @Query("order") order: String
    ): History

    @GET("erc20/{tokenAddress}/price")
    suspend fun getTokenPrice(
        @Path("tokenAddress") tokenAddress: String,
        @Query("chain") chain: String
    ): TokenPriceResponse

}