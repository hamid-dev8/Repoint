package com.repoint.sources.datarepo

import com.repoint.database.dao.NetworkDao
import com.repoint.models.sharedmodels.local.BlockchainNetworkEntity
import com.repoint.models.sharedmodels.local.BlockchainNetworkWithTokens
import com.repoint.models.sharedmodels.local.LocalActiveNetworks
import com.repoint.models.sharedmodels.local.TokenEntity
import com.repoint.sources.datarepo.datasource.NetworkDataSource
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

    override suspend fun getActiveTokens(tokenId: Int): List<TokenEntity> {
        return networkDao.getActiveTokens(tokenId)
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

    override suspend fun getActiveNetworks(): List<LocalActiveNetworks> {
        return networkDao.getActiveNetworks()
    }

    override suspend fun insertNetworks(networks: List<BlockchainNetworkEntity>) {
        return networkDao.insertNetworks(networks)
    }

    override suspend fun insertActiveNetwork(active: LocalActiveNetworks) {
        return networkDao.insertActiveNetwork(active)
    }

    override suspend fun deleteActiveNetwork(networkId: Int) {
        return networkDao.deleteActiveNetwork(networkId)
    }

}