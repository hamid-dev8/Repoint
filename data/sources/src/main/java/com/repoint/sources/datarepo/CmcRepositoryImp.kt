package com.repoint.sources.datarepo

import android.util.Log
import com.repoint.database.dao.CmcTokenDao
import com.repoint.models.sharedmodels.local.ActiveTokenKey
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

    override suspend fun fetchTokenMapPageByLimit(start: Int, limit: Int): ApiResult<CmcMapData> {
        return safeApiCall {
            api.getTokensListByLimit(start, limit)
        }
    }

    override suspend fun fetchTokenMapBySort(sort: String): ApiResult<CmcMapData> {
        return safeApiCall {
            api.getAllTokensSorted(sort)
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

    override suspend fun fetchTokenMetadataBySlug(slug : String): ApiResult<TokenInfoMetadataResponse> {
        return safeApiCall {
            api.getTokensInfoBySlug(slug)
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
            val response = api.getTokensListByLimit(start,limit)
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

    override suspend fun insertAllTokens(entities: List<CmcTokenEntity>) {
        cmcDao.insertAllTokens(entities)
    }

    override suspend fun insertActiveToken(activeNetworks: LocalActiveNetworks) {
        return cmcDao.insertActiveToken(activeNetworks)
    }

    override suspend fun getActiveTokenIds(walletId: String): Flow<List<Int>> {
        return cmcDao.getActiveTokenIds(walletId)
    }

    override suspend fun getActiveTokenKeys(walletId: String): Flow<List<ActiveTokenKey>> {
        Log.d("DAO", "Emitting active tokens for $walletId")
        return cmcDao.getActiveTokenKeys(walletId)
    }

    override suspend fun getActiveTokenEntities(walletId: String): Flow<List<LocalActiveNetworks>> {
        return cmcDao.getActiveTokenEntities(walletId)
    }

    override suspend fun deleteActiveNetworks(tokenId: Int, walletId: String,chainName : String) {
        return cmcDao.deleteActiveNetworks(tokenId,walletId,chainName)
    }

    override suspend fun getTokenCount(): Int {
        return cmcDao.countTokens()
    }

    override suspend fun getLastUpdatedTime(): Long? {
        return cmcDao.getLastUpdatedTime()
    }

    override suspend fun getDbTokensPaged(offset: Int, limit: Int): List<CmcTokenEntity> {
        return cmcDao.getTokensPaged(offset,limit)
    }

}
