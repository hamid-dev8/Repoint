package com.repoint.network.util

import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.platform.Platform
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Objects
import java.util.concurrent.TimeUnit

class RetrofitBuilder() {

    val timeOut = 10000L
    val REQUEST_TAG = "APIREQ"
    val RESPONSE_TAG = "APIRES"
    val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJub25jZSI6IjllOWYwYzQ5LTI0Y2ItNGNlYi05NDg1LWY2ZjI4NGEzODZkMSIsIm9yZ0lkIjoiNDI1ODk2IiwidXNlcklkIjoiNDM4MDYyIiwidHlwZUlkIjoiZDBmMGJhMzctM2VmNi00OGNjLWJkNjgtNmE3MzE2NGZmMzc1IiwidHlwZSI6IlBST0pFQ1QiLCJpYXQiOjE3MzY5MzA1NDgsImV4cCI6NDg5MjY5MDU0OH0.0OMiwXnG7EhekR8hM41PngKIh0T9SG5NIUk8YsmaBP8"
    private final val baseUrl: String = "https://deep-index" //TODO add proper baseUrl!!!

    companion object {

        private final val baseUrl: String = "https://deep-index" //TODO add proper baseUrl!!!
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
                        .header("accept", Objects.requireNonNull("application/json"))
                        .header("X-API-Key", API_KEY)
                        .build()

                    chain.proceed(request)

                }).addInterceptor(
                    LoggingInterceptor.Builder()
                        .setLevel(Level.BASIC)
                        .log(Platform.INFO)
                        .log(Platform.WARN)
                        .setLevel(Level.BODY) //TODO check this for logging response
                        .request(REQUEST_TAG)
                        .response(RESPONSE_TAG)
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