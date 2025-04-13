package com.repoint.sources.datarepo

import com.repoint.database.dao.AuthDao
import com.repoint.models.sharedmodels.local.ChainWallet
import com.repoint.models.sharedmodels.local.MasterWallet
import com.repoint.models.sharedmodels.local.RepointWallet
import com.repoint.sources.datarepo.datasource.AuthDataSource
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
 class WalletRepositoryImp @Inject constructor(private val authDao: AuthDao) : AuthDataSource {

    override suspend fun insertMasterWallet(masterWallet: MasterWallet) {
        authDao.insertMasterWallet(masterWallet)
    }

    override suspend fun insertChainWallets(chainWallets: List<ChainWallet>) {
        authDao.insertChainWallets(chainWallets)
    }

    override suspend fun insertChainWallet(chainWallet: ChainWallet) {
        authDao.insertChainWallet(chainWallet)
    }

    override suspend fun getChainWalletsByMaster(masterWalletId: String): List<ChainWallet> {
        return authDao.getChainWalletsByMaster(masterWalletId)
    }

    override suspend fun getChainWallet(masterWalletId: String, coinType: Int): ChainWallet? {
        return authDao.getChainWallet(masterWalletId,coinType)
    }

    override suspend fun getMasterWallet(masterWalletId: String): MasterWallet {
        return authDao.getMasterWallet(masterWalletId)
    }

    override suspend fun linkMasterWalletToUser(masterWalletId: String, userId: String) {
        authDao.linkMasterWalletToUser(masterWalletId,userId)
    }

    override suspend fun getAllMasterWallets(userId: String): List<MasterWallet> {
        return authDao.getAllMasterWallets(userId)
    }


    //////////////////////////////////////////////////////////////////////////////////////
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