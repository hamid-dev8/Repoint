package com.repoint.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.models.sharedmodels.local.TokenEntity
import com.repoint.models.sharedmodels.remote.NativesBalance
import com.repoint.models.sharedmodels.remote.TokensBalance
import com.repoint.models.sharedmodels.remote.chainInfoMap
import com.repoint.models.sharedmodels.remote.moralisChainMap
import com.repoint.sources.datarepo.datasource.NetworkDataSource
import com.repoint.sources.datarepo.datasource.TokenDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.math.log


@HiltViewModel
class TokenViewModel @Inject constructor(
    private val repository: TokenDataSource,
    private val db: NetworkDataSource
) : ViewModel() {
    private val _allTokens = MutableStateFlow<List<TokenEntity>>(emptyList())
    val allTokens: StateFlow<List<TokenEntity>> = _allTokens.asStateFlow()

    private val _nativeTokenPriceUsd = MutableStateFlow<Float?>(null)
    val nativeTokenPriceUsd: StateFlow<Float?> = _nativeTokenPriceUsd.asStateFlow()

    suspend fun getMergedActivatedTokenBalances(
        walletAddress: String,
        networkName: String,
        masterWalletId: String
    ): NativesBalance {

        val activeTokens = db.getActiveTokensNow(masterWalletId)// collect first value

        val nonZeroBalances = repository.getTokenBalance(walletAddress, chain = networkName).result

        val balanceMap = nonZeroBalances?.associateBy { it.tokenAddress.lowercase() }
        val merged = activeTokens.map { token ->
            val tokensBalance = balanceMap?.get(token.contractAddress.lowercase())
            tokensBalance ?: balanceResponseFromLocalToken(token)
        }

        return NativesBalance(
            cursor = "",
            page = 1,
            pageSize = merged.size,
            result = merged
        )
    }

    fun getAllTokens(masterWalletId: String?)
    {
        viewModelScope.launch {
            _allTokens.value = db.getAllTokens(masterWalletId)
        }
    }

    /*
        suspend fun getActivatedTokens() : List<Int>{
            return db.getActiveTokens().map { it.tokenId }
        }
    */

    fun balanceResponseFromLocalToken(token: TokenEntity): TokensBalance {
        return TokensBalance(
            tokenAddress = token.contractAddress,
            symbol = token.symbol,
            name = token.name,
            logo = token.logoUrl,
            decimals = token.decimals,
            thumbnail = token.logoUrl,
            balance = "0"
        )
    }

    fun getNativeTokenPrice(chainId : Long){
        viewModelScope.launch {
            try {

                val tokenAddress = chainInfoMap[chainId] ?: run {
                    Log.e("TokenVM", "❌ Missing wrapped token address for chainId: $chainId")
                    return@launch
                }
                val chain = moralisChainMap[chainId] ?: run {
                    Log.e("TokenVM", "❌ Missing chain string for chainId: $chainId")
                    return@launch
                }

                val priceResponse = repository.getTokenPrice(tokenAddress,chain)
                _nativeTokenPriceUsd.value = priceResponse.usdPrice
                Log.d("TokenVM", "✅ Native token USD price: ${priceResponse.usdPrice}")
            }catch (e : Exception){
                Log.e("TokenVM", "❌ Failed to fetch native token price", e)
                _nativeTokenPriceUsd.value = null
            }
        }
    }

    fun calculateGasFeeUsd(
        gasLimit: BigInteger,
        gasPriceGwei: BigDecimal,
        nativeTokenUsdPrice: Float
    ): BigDecimal {
        val gasPriceEth = gasPriceGwei.divide(BigDecimal(1_000_000_000), 18, RoundingMode.HALF_UP)
        val gasCostEth = gasPriceEth.multiply(BigDecimal(gasLimit))
        return gasCostEth.multiply((nativeTokenUsdPrice).toBigDecimal())
    }

}

