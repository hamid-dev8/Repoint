package com.repoint.sources.datarepo

import com.repoint.database.dao.CmcTokenDao
import com.repoint.models.sharedmodels.local.CmcTokenEntity
import com.repoint.models.sharedmodels.local.LocalActiveNetworks
import com.repoint.models.sharedmodels.remote.CmcAllTokens
import com.repoint.models.sharedmodels.remote.CmcMapData
import com.repoint.models.sharedmodels.remote.TokenInfoMetadataResponse
import com.repoint.models.sharedmodels.remote.TokenQuotesResponse
import com.repoint.models.sharedmodels.ui.ApiResult
import com.repoint.network.util.WebApi
import com.repoint.network.util.safeApiCall
import com.repoint.sources.datarepo.datasource.CmcDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CmcRepositoryImp @Inject constructor(
    private val api: WebApi,
    private val cmcDao: CmcTokenDao
) : CmcDataSource {

    override suspend fun fetchTokenMapPage(start: Int, limit: Int): ApiResult<CmcMapData> {
        return safeApiCall {
            api.getAllTokensList(start, limit)
        }
    }

    override suspend fun fetchTokenMapBySymbol(symbol: String): ApiResult<List<CmcAllTokens>> {
        return safeApiCall {
            api.getTokensBySymbol(symbol.uppercase()).data
        }
    }

    override suspend fun fetchTokenMetadata(ids: List<Int>): ApiResult<TokenInfoMetadataResponse> {
        return safeApiCall {
            val joinedIds = ids.joinToString(",")
            api.getTokensInfo(joinedIds)
        }
    }

    override suspend fun fetchTokenPrices(ids: List<Int>): ApiResult<TokenQuotesResponse> {
       return safeApiCall {
            val idParam = ids.joinToString(",")
            api.getTokenPrices(idParam)
        }
    }

    override suspend fun getCachedTokens(): List<CmcTokenEntity> {
        return cmcDao.getAllTokens()
    }
    override suspend fun cacheMapDataPage(start: Int, limit: Int): ApiResult<List<CmcAllTokens>> {
        return safeApiCall {
            val response = api.getAllTokensList(start,limit)
            val mapped = response.data.map { map ->
                CmcTokenEntity(
                    id = map.id,
                    rank = map.rank,
                    name = map.name,
                    symbol = map.symbol,
                    slug = map.slug,
                    isActive = map.isActive,
                    firstHistoricalData = map.firstHistoricalData,
                    lastHistoricalData = map.lastHistoricalData,
                    platformId = map.platform?.id,
                    platformName = map.platform?.name,
                    platformSymbol = map.platform?.symbol,
                    platformSlug = map.platform?.slug,
                    tokenAddress = map.platform?.tokenAddress,
                    logo = null, // filled later from /info
                    description = null,
                    websiteUrl = null,
                    lastUpdated = System.currentTimeMillis()
                )            }
            cmcDao.insertAllTokens(mapped)
            response.data
        }
    }

    override suspend fun searchTokensByQuery(query: String): List<CmcTokenEntity> {
        return cmcDao.searchTokensByQuery(query)
    }

    override suspend fun insertActiveToken(activeNetworks: LocalActiveNetworks) {
        return cmcDao.insertActiveToken(activeNetworks)
    }

    override suspend fun getActiveTokenIds(walletId: String): Flow<List<Int>> {
        return cmcDao.getActiveTokenIds(walletId)
    }

    override suspend fun deleteActiveNetworks(tokenId: Int, walletId: String) {
        return cmcDao.deleteActiveNetworks(tokenId,walletId)
    }

}
