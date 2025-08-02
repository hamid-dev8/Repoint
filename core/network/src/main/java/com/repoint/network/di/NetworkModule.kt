package com.repoint.network.di

import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.repoint.models.sharedmodels.rpc.AlchemyChain
import com.repoint.network.util.NetworkApiService
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
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    val timeOut = 10000L
    val REQUEST_TAG = "APIREQ"
    val REQUEST_TAG_ALCHEMY = "APIREQAL"
    val RESPONSE_TAG_ALCHEMY = "APIRESAL"
    val RESPONSE_TAG = "APIRES"
    val API_KEY = "92f9f4c3-0574-49b9-8767-85af50ccfc0b"
    private const val ALCHEMY_API_KEY = "bAyoxiiQWwUCS2jJdMZ9hVkoKPZwD9dB"
    private  val cmcUrl: String = "https://pro-api.coinmarketcap.com" //TODO add proper baseUrl!!!
    private val networkUrl : String = "https://eth-mainnet.g.alchemy.com/v2/$ALCHEMY_API_KEY/" //Todo add repoint url

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor() : HttpLoggingInterceptor{
        val httpLogging = HttpLoggingInterceptor()
        httpLogging.level = HttpLoggingInterceptor.Level.BODY
        return httpLogging
    }


    @Provides
    @Singleton
    @CmcOkHttp
    fun getUnsafeCoinMarketCapOkHttpClient(): OkHttpClient {
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
                        .header("X-CMC_PRO_API_KEY", API_KEY)
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
    @NetworkOkHttp
    fun getUnsafeNetworkApiOkHttpClient() : OkHttpClient{
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
                        .header("User-Agent", "Mozilla/5.0") // Fix for 403
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(
                    LoggingInterceptor.Builder()
                        .setLevel(Level.BASIC)
                        .log(Platform.INFO)
                        .log(Platform.WARN)
                        .setLevel(Level.BODY)
                        .request(REQUEST_TAG_ALCHEMY)
                        .response(RESPONSE_TAG_ALCHEMY)
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
    @CmcRetrofit
    fun provideMoralisRetrofit(@CmcOkHttp okHttpClient: OkHttpClient) : Retrofit {
        return Retrofit.Builder()
            .baseUrl(cmcUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    @NetworkRetrofit
    fun provideNetworkRetrofit(@NetworkOkHttp okHttpClient  : OkHttpClient) : Retrofit{
        val defaultChain = AlchemyChain.POLYGON
        val url = getAlchemyBaseUrl(defaultChain)
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideWebApi(@CmcRetrofit retrofit: Retrofit):WebApi = retrofit.create(WebApi::class.java)

    @Provides
    @Singleton
    fun provideNetworkApiService(@NetworkRetrofit retrofit: Retrofit): NetworkApiService = retrofit.create(NetworkApiService::class.java)

    fun getAlchemyBaseUrl(chain: AlchemyChain): String {
        return "${chain.baseUrl}$ALCHEMY_API_KEY/"
    }

    private fun alchemyBaseUrl(chainId: Int): String = when (chainId) {
        1 -> "https://eth-mainnet.g.alchemy.com/v2/$ALCHEMY_API_KEY/"
        137 -> "https://polygon-mainnet.g.alchemy.com/v2/$ALCHEMY_API_KEY/"
        56 -> "https://bsc-mainnet.g.alchemy.com/v2/$ALCHEMY_API_KEY/" // or another provider
        else -> error("Unsupported chain ID")
    }

    @Provides
    @Named("alchemy_eth")
    fun provideAlchemyEthRetrofit(@NetworkOkHttp okHttpClient: OkHttpClient): NetworkApiService {
        return Retrofit.Builder()
            .baseUrl(alchemyBaseUrl(1))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NetworkApiService::class.java)
    }

    @Provides
    @Named("alchemy_polygon")
    fun provideAlchemyPolygonRetrofit(@NetworkOkHttp okHttpClient: OkHttpClient): NetworkApiService {
        return Retrofit.Builder()
            .baseUrl(alchemyBaseUrl(137))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NetworkApiService::class.java)
    }

    @Provides
    @Named("alchemy_bnb")
    fun provideAlchemyBnbRetrofit(@NetworkOkHttp okHttpClient: OkHttpClient): NetworkApiService {
        return Retrofit.Builder()
            .baseUrl(alchemyBaseUrl(56))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NetworkApiService::class.java)
    }

  /*  @Provides
    @Singleton
    fun provideTokenSource(api : WebApi) : TokenDataSource{
        return TokenBalanceRepositoryImp(api)
    }*/

}