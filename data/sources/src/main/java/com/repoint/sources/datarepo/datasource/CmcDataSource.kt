package com.repoint.sources.datarepo.datasource

import com.repoint.models.sharedmodels.local.ActiveTokenKey
import com.repoint.models.sharedmodels.local.CmcTokenEntity
import com.repoint.models.sharedmodels.local.LocalActiveNetworks
import com.repoint.models.sharedmodels.remote.CmcAllTokens
import com.repoint.models.sharedmodels.remote.CmcMapData
import com.repoint.models.sharedmodels.remote.TokenInfoMetadataResponse
import com.repoint.models.sharedmodels.remote.TokenQuotesResponse
import com.repoint.models.sharedmodels.ui.ApiResult
import kotlinx.coroutines.flow.Flow

interface CmcDataSource {
    /* //api + db
     suspend fun loadNextTokenPage() : ApiResult<List<CmcTokenEntity>>

     suspend fun getMapTokens() : ApiResult<List<CmcMapData>>

     suspend fun fetchAllTokensFromApi() : ApiResult<List<CmcTokenEntity>>
*/
    //just api
    // Fetch 12-by-12 paginated raw map data
    suspend fun fetchTokenMapPageByLimit(
        start: Int,
        limit: Int = 12
    ): ApiResult<CmcMapData>

    //get the sorted /map

    suspend fun fetchTokenMapBySort(
        sort: String
    ): ApiResult<CmcMapData>

    //get map token  by symbol
    suspend fun fetchTokenMapBySymbol(
        symbol: String
    ): ApiResult<List<CmcAllTokens>>

    // Fetch metadata info for a chunk of token IDs
    suspend fun fetchTokenMetadata(
        ids: List<Int>
    ): ApiResult<TokenInfoMetadataResponse>

    suspend fun fetchTokenMetadataBySlug(
        slug: String
    ): ApiResult<TokenInfoMetadataResponse>

    suspend fun fetchTokenPrices(
        ids: List<Int>
    ): ApiResult<TokenQuotesResponse>

    suspend fun getNativeTokenPriceBySymbol(
        symbol: String
    ): ApiResult<Double>


    //db
    suspend fun getCachedTokens(): List<CmcTokenEntity>
    suspend fun cacheMapDataPage(start: Int, limit: Int): ApiResult<List<CmcAllTokens>>
    suspend fun searchTokensByQuery(query: String): List<CmcTokenEntity>
    suspend fun insertAllTokens(entities: List<CmcTokenEntity>)


    //active tokens in db
    suspend fun insertActiveToken(activeNetworks: LocalActiveNetworks)
    suspend fun getActiveTokenIds(walletId: String): Flow<List<Int>>
    suspend fun getActiveTokenKeys(walletId: String): Flow<List<ActiveTokenKey>>
    suspend fun getActiveTokenEntities(walletId: String): Flow<List<LocalActiveNetworks>>
    suspend fun deleteActiveNetworks(tokenId: Int, walletId: String, chainName: String)

    //time of db
    suspend fun getTokenCount(): Int
    suspend fun getLastUpdatedTime(): Long?
    suspend fun getDbTokensPaged(offset: Int, limit: Int): List<CmcTokenEntity>
}