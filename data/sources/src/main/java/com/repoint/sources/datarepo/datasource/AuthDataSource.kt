package com.repoint.sources.datarepo.datasource

import com.repoint.models.sharedmodels.local.RepointWallet

interface AuthDataSource
{
    suspend fun authWallet(wallet : RepointWallet)
    suspend fun getAllWallets(userId: String) : List<RepointWallet>
    suspend fun getWallet(walletId : String) : RepointWallet
    suspend fun linkWalletToUser(walletId : String,userId : String)

}