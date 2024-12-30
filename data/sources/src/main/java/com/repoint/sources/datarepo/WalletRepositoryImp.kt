package com.repoint.sources.datarepo

import com.repoint.database.dao.AuthDao
import com.repoint.models.sharedmodels.RepointWallet
import com.repoint.models.sharedmodels.User
import com.repoint.sources.datarepo.datasource.AuthDataSource
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
 class WalletRepositoryImp @Inject constructor(private val authDao: AuthDao) : AuthDataSource {


    override suspend fun authWallet(wallet : RepointWallet) {
        authDao.authWallet(wallet)
    }

    override suspend fun getWallet(id : String): RepointWallet {
        return authDao.getWallet(id)
    }
}