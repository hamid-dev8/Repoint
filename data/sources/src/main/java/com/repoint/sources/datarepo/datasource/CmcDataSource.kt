package com.repoint.sources.datarepo.datasource

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
    suspend fun fetchTokenMapPage(
        start: Int,
        limit: Int = 12
    ): ApiResult<CmcMapData>

    //get map token  by symbol
    suspend fun fetchTokenMapBySymbol(
        symbol : String
    ) : ApiResult<List<CmcAllTokens>>

    // Fetch metadata info for a chunk of token IDs
    suspend fun fetchTokenMetadata(
        ids: List<Int>
    ): ApiResult<TokenInfoMetadataResponse>

    suspend fun fetchTokenPrices(
        ids: List<Int>
    ): ApiResult<TokenQuotesResponse>

    //db
    suspend fun getCachedTokens(): List<CmcTokenEntity>
    suspend fun cacheMapDataPage(start: Int, limit: Int): ApiResult<List<CmcAllTokens>>
    suspend fun searchTokensByQuery(query: String): List<CmcTokenEntity>


    //active tokens in db
    suspend fun insertActiveToken(activeNetworks: LocalActiveNetworks)
    suspend fun getActiveTokenIds(walletId: String): Flow<List<Int>>
    suspend fun deleteActiveNetworks(tokenId: Int, walletId: String)

}