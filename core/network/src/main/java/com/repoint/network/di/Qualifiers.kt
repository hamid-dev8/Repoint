package com.repoint.network.di

import com.repoint.models.sharedmodels.rpc.AlchemyChain
import dagger.MapKey
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CmcOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CmcRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkRetrofit


//chains

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EthereumRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PolygonRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BscRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ArbitrumRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AlchemyRetrofitMap

@MapKey
annotation class AlchemyChainKey(val value: AlchemyChain)

