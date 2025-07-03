package com.repoint.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/*
@Module
@InstallIn(SingletonComponent::class)
object AlchemyRetrofitModule {

    private const val TIMEOUT = 10_000L
    private const val ALCHEMY_API_KEY = "bAyoxiiQWwUCS2jJdMZ9hVkoKPZwD9dB"

    @Provides
    private fun provideCommonOkHttp(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @EthereumRetrofit
    fun provideEthereumRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://eth-mainnet.g.alchemy.com/v2/$ALCHEMY_API_KEY/")
        .client(provideCommonOkHttp())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @PolygonRetrofit
    fun providePolygonRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://polygon-mainnet.g.alchemy.com/v2/$ALCHEMY_API_KEY/")
        .client(provideCommonOkHttp())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @BscRetrofit
    fun provideBscRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://bnb-mainnet.g.alchemy.com/v2/$ALCHEMY_API_KEY/") // Replace if needed
        .client(provideCommonOkHttp())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

}*/
