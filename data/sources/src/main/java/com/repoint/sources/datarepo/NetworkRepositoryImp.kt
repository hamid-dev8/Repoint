package com.repoint.sources.datarepo

import android.util.Log
import com.repoint.database.dao.NetworkDao
import com.repoint.models.sharedmodels.local.BlockchainNetworkEntity
import com.repoint.models.sharedmodels.local.BlockchainNetworkWithTokens
import com.repoint.models.sharedmodels.local.LocalActiveNetworks
import com.repoint.models.sharedmodels.local.TokenEntity
import com.repoint.models.sharedmodels.local.TokenWithNetwork
import com.repoint.sources.datarepo.datasource.NetworkDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NetworkRepositoryImp @Inject constructor(private val networkDao : NetworkDao) :  NetworkDataSource
{

    override suspend fun isDatabaseEmpty(): Boolean {
        return networkDao.getNetworkCount() == 0
    }

    override suspend fun insertToken(tokens : List<TokenEntity>) {
        return networkDao.insertTokens(tokens)
    }

   override suspend fun getTokensForNetwork(networkId: Int): List<TokenEntity> {
        return networkDao.getTokensForNetwork(networkId)
    }

    override suspend fun getAllTokens(masterWalletId: String?): List<TokenEntity> {
        return networkDao.getAllTokens(masterWalletId)
    }

    override suspend fun getTokensWithNetwork(tokenId: Int): TokenWithNetwork {
        return networkDao.getTokenWithNetwork(tokenId)
    }

    override suspend fun getActiveTokensWithNetworks(masterWalletId: String): List<TokenWithNetwork> {
        return networkDao.getActiveTokensWithNetworks(masterWalletId)
    }

    override suspend fun getNetworkByTokenId(tokenId: Int): BlockchainNetworkEntity? {
        return networkDao.getNetworkByTokenId(tokenId)
    }

    override suspend fun getActiveTokens(walletId : String): Flow<List<TokenEntity>> {
        return networkDao.getActiveTokens(walletId)
    }

    override suspend fun getActiveTokensNow(walletId: String): List<TokenEntity> {
        return networkDao.getActiveTokensNow(walletId)
    }

    override suspend fun getNetworkWithTokens() : List<BlockchainNetworkWithTokens> {
        return networkDao.getNetworksWithTokens()
    }

    override suspend fun getNetworkById(networkId: Int): BlockchainNetworkEntity {
        return networkDao.getNetworkById(networkId)
    }

    override suspend fun getAllNetworks(): List<BlockchainNetworkEntity> {
        return networkDao.getAllNetworks()
    }

    override suspend fun getActiveNetworks(walletId : String): List<LocalActiveNetworks> {
        return networkDao.getActiveNetworks(walletId)
    }

    override suspend fun debugActiveNetworks(masterWalletId: String): List<LocalActiveNetworks> {
        val all = networkDao.getAllActiveNetworksDebug(masterWalletId = masterWalletId)
        Log.d("DEBUG_ACTIVE_NETS", "All active networks on $masterWalletId:\n" + all.joinToString("\n") { it.toString() })
        return all
    }




    override suspend fun insertNetworks(networks: List<BlockchainNetworkEntity>) {
        return networkDao.insertNetworks(networks)
    }

    override suspend fun getActiveTokensId(walletId: Int): Flow<LocalActiveNetworks> {
        TODO("Not yet implemented")
    }

    override suspend fun insertActiveNetwork(active: LocalActiveNetworks) {
        return networkDao.insertActiveNetwork(active)
    }

    override suspend fun deleteActiveNetwork(networkId: Int,masterWalletId : String) {
        return networkDao.deleteActiveNetwork(networkId,masterWalletId)
    }

}