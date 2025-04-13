package com.repoint.sources.datarepo.datasource

import com.repoint.models.sharedmodels.local.ChainWallet
import com.repoint.models.sharedmodels.local.MasterWallet
import com.repoint.models.sharedmodels.local.RepointWallet

interface AuthDataSource
{

    suspend fun insertMasterWallet(masterWallet: MasterWallet)
    suspend fun insertChainWallets(chainWallets : List<ChainWallet>)
    suspend fun insertChainWallet(chainWallet: ChainWallet)
    suspend fun getChainWalletsByMaster(masterWalletId : String) : List<ChainWallet>
    suspend fun getChainWallet(masterWalletId : String,coinType : Int) : ChainWallet?
    suspend fun getMasterWallet(masterWalletId : String) : MasterWallet
    suspend fun linkMasterWalletToUser(masterWalletId: String,userId: String)
    suspend fun getAllMasterWallets(userId: String) : List<MasterWallet>

    ///////////////////////////////////////////////////////////////////


    suspend fun authWallet(wallet : RepointWallet)
    suspend fun getAllWallets(userId: String) : List<RepointWallet>
    suspend fun getWallet(walletId : String) : RepointWallet
    suspend fun linkWalletToUser(walletId : String,userId : String)

}