package com.repoint.network.di

import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.repoint.network.TokenBalanceRepositoryImp
import com.repoint.network.datasource.TokenDataSource
import com.repoint.network.util.WebApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.platform.Platform
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    val timeOut = 10000L
    val REQUEST_TAG = "APIREQ"
    val RESPONSE_TAG = "APIRES"
    val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJub25jZSI6IjllOWYwYzQ5LTI0Y2ItNGNlYi05NDg1LWY2ZjI4NGEzODZkMSIsIm9yZ0lkIjoiNDI1ODk2IiwidXNlcklkIjoiNDM4MDYyIiwidHlwZUlkIjoiZDBmMGJhMzctM2VmNi00OGNjLWJkNjgtNmE3MzE2NGZmMzc1IiwidHlwZSI6IlBST0pFQ1QiLCJpYXQiOjE3MzY5MzA1NDgsImV4cCI6NDg5MjY5MDU0OH0.0OMiwXnG7EhekR8hM41PngKIh0T9SG5NIUk8YsmaBP8"
    private  val baseUrl: String = "https://deep-index.moralis.io/api/v2.2/" //TODO add proper baseUrl!!!

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor() : HttpLoggingInterceptor{
        val httpLogging = HttpLoggingInterceptor()
        httpLogging.level = HttpLoggingInterceptor.Level.BODY
        return httpLogging
    }


    @Provides
    @Singleton
    fun getUnsafeOkHttpClient(): OkHttpClient {
        return try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            val sslSocketFactory = sslContext.socketFactory

            OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(timeOut, TimeUnit.MILLISECONDS)
                .addInterceptor { chain ->
                    val original: Request = chain.request()
                    val request: Request = original.newBuilder()
                        .header("accept", "application/json")
                        .header("X-API-Key", API_KEY)
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(
                    LoggingInterceptor.Builder()
                        .setLevel(Level.BASIC)
                        .log(Platform.INFO)
                        .log(Platform.WARN)
                        .setLevel(Level.BODY)
                        .request(REQUEST_TAG)
                        .response(RESPONSE_TAG)
                        .build()
                )
                .addInterceptor(provideHttpLoggingInterceptor())
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient) : Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideWebApi(retrofit: Retrofit):WebApi = retrofit.create(WebApi::class.java)

  /*  @Provides
    @Singleton
    fun provideTokenSource(api : WebApi) : TokenDataSource{
        return TokenBalanceRepositoryImp(api)
    }*/

}