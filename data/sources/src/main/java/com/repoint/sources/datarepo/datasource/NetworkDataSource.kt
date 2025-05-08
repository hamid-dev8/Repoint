package com.repoint.sources.datarepo.datasource

import com.repoint.models.sharedmodels.local.BlockchainNetworkEntity
import com.repoint.models.sharedmodels.local.BlockchainNetworkWithTokens
import com.repoint.models.sharedmodels.local.LocalActiveNetworks
import com.repoint.models.sharedmodels.local.TokenEntity
import com.repoint.models.sharedmodels.local.TokenWithNetwork
import kotlinx.coroutines.flow.Flow

interface NetworkDataSource
{
    suspend fun isDatabaseEmpty() : Boolean
    suspend fun insertToken(tokens : List<TokenEntity>)
    suspend fun getTokensForNetwork(networkId: Int): List<TokenEntity>
    suspend fun getAllTokens(masterWalletId: String?) : List<TokenEntity>
    suspend fun getTokensWithNetwork(tokenId : Int) : TokenWithNetwork

    suspend fun getNetworkByTokenId(tokenId: Int) : BlockchainNetworkEntity?

    //suspend fun getActiveTokens() : List<TokenEntity>
    suspend fun getActiveTokens(walletId : String) : Flow<List<TokenEntity>>
    suspend fun getActiveTokensNow(walletId: String) : List<TokenEntity>

    suspend fun getNetworkWithTokens() : List<BlockchainNetworkWithTokens>
    suspend fun getAllNetworks()  : List<BlockchainNetworkEntity>
    suspend fun getNetworkById(networkId : Int) : BlockchainNetworkEntity
    suspend fun getActiveNetworks(walletId: String) : List<LocalActiveNetworks>
    suspend fun insertNetworks(networks: List<BlockchainNetworkEntity>)
    suspend fun insertActiveNetwork(active : LocalActiveNetworks)
    suspend fun deleteActiveNetwork(networkId : Int,masterWalletId : String)

    //debug
    suspend fun debugActiveNetworks(masterWalletId: String) : List<LocalActiveNetworks>

}