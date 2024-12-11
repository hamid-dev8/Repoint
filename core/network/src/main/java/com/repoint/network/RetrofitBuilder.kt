package com.repoint.network

import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import kotlinx.coroutines.channels.Channel
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.platform.Platform
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Objects
import java.util.concurrent.TimeUnit

class RetrofitBuilder() {

    val timeOut = 10000L
    val requestTag = "APIREQ"
    val responseTag = "APIRES"

    companion object {

        private final val baseUrl: String = "" //TODO add proper baseUrl!!!
        var INSTANCE: RetrofitBuilder? = null

        lateinit var webApi: WebApi
        lateinit var mockWebApi: WebApi
    }

    init {

        if (INSTANCE == null) {
            val httpLogging = HttpLoggingInterceptor()
            httpLogging.setLevel(HttpLoggingInterceptor.Level.BODY)


            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(timeOut, TimeUnit.MILLISECONDS)
                .addInterceptor(Interceptor { chain ->

                    val original: Request = chain.request()
                    val request: Request = original.newBuilder()
                        .header(
                            "uuid",
                            Objects.requireNonNull("todo add Sp manager")
                        ) //TODO ADD SPMANAGER GET TOKEN AND ETC...
                        .header("Authorization", "get token from spManager")
                        .build()

                    chain.proceed(request)

                }).addInterceptor(
                    LoggingInterceptor.Builder()
                        .setLevel(Level.BASIC)
                        .log(Platform.INFO)
                        .log(Platform.WARN)
                        .setLevel(Level.BODY) //TODO check this for logging response
                        .request(requestTag)
                        .response(responseTag)
                        .build()
                ).build()


            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

            webApi = retrofit.create(WebApi::class.java)

            //create Mock Retrofit
            //TODO ADD MOCK


            INSTANCE = RetrofitBuilder()
        }
    }

    fun getWebApi() = webApi
}