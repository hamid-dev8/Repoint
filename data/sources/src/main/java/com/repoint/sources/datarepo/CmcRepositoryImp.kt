package com.repoint.sources.datarepo

import com.repoint.database.dao.CmcTokenDao
import com.repoint.models.sharedmodels.local.CmcTokenEntity
import com.repoint.network.util.WebApi
import com.repoint.sources.datarepo.datasource.CmcDataSource
import javax.inject.Inject

class CmcRepositoryImp @Inject constructor(private val api : WebApi,private val cmcDao : CmcTokenDao) : CmcDataSource {


    override suspend fun getAllTokens(): List<CmcTokenEntity> {
        val now = System.currentTimeMillis()
        val lastUpdated = cmcDao.getLastUpdateTime()
        val threeDaysMs = 3 * 24 * 60 * 60 * 1000

        return if (lastUpdated == null || now - lastUpdated > threeDaysMs)
        {
            //fetch from network
            val mapResponse = api.getAllTokensList()
            val ids = mapResponse.data.map { it.id.toString() }
            val batched = ids.chunked(100) // chunk the cmc safe

            val infoResponses = batched.map { chunk ->
                api.getTokensInfo(chunk.joinToString ( "," ))
            }

            val infoMap = infoResponses.flatMap { it.data.entries }
                .associateBy({it.key.toInt()},{it.value})

            val tokens = mapResponse.data.map{
                val info = infoMap[it.id]

                CmcTokenEntity(
                    id = it.id,
                    rank = it.rank,
                    name = it.name,
                    symbol = it.symbol,
                    slug = it.slug,
                    isActive = it.isActive,
                    firstHistoricalData = it.firstHistoricalData,
                    lastHistoricalData = it.lastHistoricalData,
                    platformId = it.platform?.id,
                    platformName = it.platform?.name,
                    platformSymbol = it.platform?.symbol,
                    platformSlug = it.platform?.slug,
                    tokenAddress = it.platform?.tokenAddress,
                    logo = info?.logo,
                    description = info?.description,
                    websiteUrl = info?.urls?.website?.firstOrNull(),
                    lastUpdated = now
                )
            }
            cmcDao.clearAll()
            cmcDao.insertAllTokens(tokens = tokens)
            tokens
        }else{
            //load from db if the time tolerance of the three days doesnt gets passed yet!
            cmcDao.getAllTokens()
        }

    }


}