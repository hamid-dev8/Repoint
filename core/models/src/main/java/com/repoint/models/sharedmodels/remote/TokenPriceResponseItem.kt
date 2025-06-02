package com.repoint.models.sharedmodels.remote

data class TokenPriceResponseItem(val tokenName: String,
                                  val tokenSymbol: String,
                                  val tokenLogo: String,
                                  val tokenDecimals: String,
                                  val nativePrice: NativePrice,
                                  val usdPrice: Double?,
                                  val usdPriceFormatted: String?,
                                  val exchangeName: String?,
                                  val exchangeAddress: String?,
                                  val tokenAddress: String,
                                  val priceLastChangedAtBlock: String?,
                                  val blockTimestamp: String?,
                                  val possibleSpam: Boolean,
                                  val verifiedContract: Boolean,
                                  val pairAddress: String?,
                                  val pairTotalLiquidityUsd: String?,
                                  val securityScore: Int?,
                                  val usdPrice24hr: Double?,
                                  val usdPrice24hrUsdChange: Double?,
                                  val usdPrice24hrPercentChange: Double?,
                                  val `24hrPercentChange`: String?)

data class NativePrice(
    val value: String,
    val decimals: Int,
    val name: String,
    val symbol: String,
    val address: String
)
