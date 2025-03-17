package com.repoint.sources.datarepo

import com.repoint.models.sharedmodels.remote.History
import com.repoint.sources.datarepo.datasource.HistoryDataSource
import com.repoint.network.util.WebApi
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class HistoryRepositoryImp @Inject constructor(private val webApi : WebApi) : HistoryDataSource {

    override suspend fun getNativeHistory(address : String,chain  : String,order : String): History {
            return webApi.getNativeHistory(address,chain,order)
    }

}