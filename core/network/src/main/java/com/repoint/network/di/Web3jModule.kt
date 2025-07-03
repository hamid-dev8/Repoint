package com.repoint.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Web3jModule {

/*
    //private const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJub25jZSI6IjllOWYwYzQ5LTI0Y2ItNGNlYi05NDg1LWY2ZjI4NGEzODZkMSIsIm9yZ0lkIjoiNDI1ODk2IiwidXNlcklkIjoiNDM4MDYyIiwidHlwZUlkIjoiZDBmMGJhMzctM2VmNi00OGNjLWJkNjgtNmE3MzE2NGZmMzc1IiwidHlwZSI6IlBST0pFQ1QiLCJpYXQiOjE3MzY5MzA1NDgsImV4cCI6NDg5MjY5MDU0OH0.0OMiwXnG7EhekR8hM41PngKIh0T9SG5NIUk8YsmaBP8"
    //private const val NODE_URL = "https://speedy-nodes-nyc.moralis.io/$API_KEY/eth/mainnet"
    private const val NODE_URL_ETH =
        "https://eth-sepolia.g.alchemy.com/v2/bAyoxiiQWwUCS2jJdMZ9hVkoKPZwD9dB"
    private const val NODE_URL_POL =
        "https://site1.moralis-nodes.com/polygon/95c7139ee35e410daae09e53d1725518"

*/


    @Provides
    @Singleton
    fun provideWeb3Provider(): Web3Provider {
        return Web3Provider()
    }

}