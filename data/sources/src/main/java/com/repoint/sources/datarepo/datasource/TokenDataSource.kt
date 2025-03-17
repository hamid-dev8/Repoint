package com.repoint.sources.datarepo.datasource

import com.repoint.models.sharedmodels.remote.NativesBalance

interface TokenDataSource {

    suspend fun getTokenBalance(address: String, chain: String) : NativesBalance

}