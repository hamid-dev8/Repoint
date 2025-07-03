package com.repoint.network.di

import com.repoint.models.sharedmodels.rpc.AlchemyChain
import com.repoint.network.util.NetworkApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
/*

@Module
@InstallIn(SingletonComponent::class)
object AlchemyRetrofitMapModule {

    @Provides
    @AlchemyRetrofitMap
    @JvmSuppressWildcards // ✅ Add this to remove wildcards from the Map value type
    fun provideAlchemyClientMap(
        @EthereumRetrofit ethRetrofit: Retrofit,
        @PolygonRetrofit polygonRetrofit: Retrofit,
        @BscRetrofit bscRetrofit: Retrofit,
    ): Map<AlchemyChain, NetworkApiService> {
        return mapOf(
            AlchemyChain.ETHEREUM to ethRetrofit.create(NetworkApiService::class.java),
            AlchemyChain.POLYGON to polygonRetrofit.create(NetworkApiService::class.java),
            AlchemyChain.BNB to bscRetrofit.create(NetworkApiService::class.java),
        )
    }

}*/
