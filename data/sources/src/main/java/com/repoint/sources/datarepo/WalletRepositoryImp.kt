package com.repoint.sources.datarepo

import com.repoint.database.dao.AuthDao
import com.repoint.models.sharedmodels.local.RepointWallet
import com.repoint.sources.datarepo.datasource.AuthDataSource
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
 class WalletRepositoryImp @Inject constructor(private val authDao: AuthDao) : AuthDataSource {


    override suspend fun authWallet(wallet : RepointWallet) {
        authDao.authWallet(wallet)
    }

    override suspend fun getAllWallets(userId: String): List<RepointWallet> {
        return authDao.getAllWallets(userId)
    }

    override suspend fun getWallet(walletId : String): RepointWallet {
        return authDao.getWallet(walletId)
    }

    override suspend fun linkWalletToUser(walletId: String, userId: String) {
        return authDao.linkWalletToUser(walletId,userId)
    }

}