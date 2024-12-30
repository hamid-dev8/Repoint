package com.repoint.sources.datarepo.datasource

import com.repoint.models.sharedmodels.RepointWallet
import com.repoint.models.sharedmodels.User

interface AuthDataSource
{
    suspend fun authWallet(wallet : RepointWallet)
    suspend fun getWallet(id : String) : RepointWallet

}