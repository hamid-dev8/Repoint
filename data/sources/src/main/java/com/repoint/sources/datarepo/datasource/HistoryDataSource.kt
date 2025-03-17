package com.repoint.sources.datarepo.datasource

import com.repoint.models.sharedmodels.remote.History

interface HistoryDataSource
{
    suspend fun getNativeHistory(address : String,chain : String,order : String) : History
}