package com.repoint.network.util

import com.repoint.models.sharedmodels.rpc.AlchemyChain

fun interface AlchemyClientFactory {
    fun getClient(chain: AlchemyChain): NetworkApiService
}