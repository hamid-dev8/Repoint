package com.repoint.basics.logic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
        coinType = this.coinType,
        tokens = tokens.map { tokenEntity ->
            Token(tokenEntity.tokenId,tokenEntity.name,tokenEntity.symbol,tokenEntity.contractAddress,tokenEntity.decimals,tokenEntity.logoUrl)
        } // Tokens are fetched separately
    )
}

fun TokenEntity.toToken(): Token {
    return Token(
        tokenId = this.tokenId,
        name = this.name,
        symbol = this.symbol,
        contractAddress = this.contractAddress,
        decimals = this.decimals,
        logoUrl = this.logoUrl
    )
}

@Composable
fun Modifier.clickableWithRipple(onClick:() -> Unit) : Modifier{
    return this.clickable(
        indication = ripple(bounded = true),
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
}
