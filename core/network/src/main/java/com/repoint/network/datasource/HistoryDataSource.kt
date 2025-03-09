package com.repoint.network.datasource

import com.repoint.models.sharedmodels.remote.History
import com.repoint.models.sharedmodels.remote.RepointTransactions

interface HistoryDataSource
{
    suspend fun getNativeHistory(address : String,chain : String,order : String) : History
}