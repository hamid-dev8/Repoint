package com.repoint.basics.logic

import com.repoint.models.sharedmodels.local.BlockchainNetworkEntity
import com.repoint.models.sharedmodels.local.TokenEntity
import com.repoint.models.sharedmodels.remote.BlockchainNetwork
import com.repoint.models.sharedmodels.remote.Token


fun BlockchainNetworkEntity.toDomainModel(tokens : List<TokenEntity>): BlockchainNetwork {
    return BlockchainNetwork(
        id = this.id,
        name = this.name,
        chainId = this.chainId,
        rpcUrl = this.rpcUrl,
        explorerUrl = this.explorerUrl,
        nativeToken = this.nativeToken,
        dexRouter = this.dexRouter,
        tokens = tokens.map { tokenEntity ->
            Token(tokenEntity.tokenId,tokenEntity.name,tokenEntity.symbol,tokenEntity.contractAddress,tokenEntity.decimals,tokenEntity.logoUrl)
        } // Tokens are fetched separately
    )
}