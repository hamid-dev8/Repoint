package com.repoint.network.di

import com.repoint.models.sharedmodels.rpc.AlchemyChain
import com.repoint.network.util.NetworkApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AlchemyClientFactory @Inject constructor(
    @NetworkOkHttp private val okHttpClient : OkHttpClient
) {


    private val retrofitCache = mutableMapOf<AlchemyChain, NetworkApiService>()
    private val ALCHEMY_API_KEY = "bAyoxiiQWwUCS2jJdMZ9hVkoKPZwD9dB"

    private fun buildRetrofit(baseUrl : String) : Retrofit {

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }

    fun getClient(chain: AlchemyChain): NetworkApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(getAlchemyBaseUrl(chain)) // different base URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        return retrofit.create(NetworkApiService::class.java)
    }

    private fun getAlchemyBaseUrl(chain: AlchemyChain): String {
        return "${chain.baseUrl}$ALCHEMY_API_KEY/" // ✅ ends with "/"
    }
}