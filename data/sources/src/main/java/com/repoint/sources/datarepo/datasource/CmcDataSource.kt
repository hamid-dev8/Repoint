package com.repoint.sources.datarepo.datasource

import com.repoint.models.sharedmodels.local.CmcTokenEntity

interface CmcDataSource
{
        suspend fun getAllTokens() : List<CmcTokenEntity>
}