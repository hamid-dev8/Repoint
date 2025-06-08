package com.repoint.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.models.sharedmodels.local.TokenEntity
import com.repoint.models.sharedmodels.remote.BalanceByWallet
import com.repoint.models.sharedmodels.remote.NativesBalance
import com.repoint.models.sharedmodels.remote.TokenPriceRequestItem
import com.repoint.models.sharedmodels.remote.TokensBalance
import com.repoint.models.sharedmodels.remote.chainInfoMap
import com.repoint.models.sharedmodels.remote.moralisChainMap
import com.repoint.models.sharedmodels.ui.UiState
import com.repoint.sources.datarepo.datasource.NetworkDataSource
import com.repoint.sources.datarepo.datasource.TokenDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import okio.IOException
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

    val _uiState = MutableStateFlow<UiState<List<TokensBalance>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()


    fun getAllTokensList() {
        viewModelScope.launch {
            val response = repository.getAllTokensList()
        }
    }

    fun getAvailableTokensFromMoralisOnly(
        walletAddress: String,
        masterWalletId: String,
        enabledChains: List<String>,
        onResult: (List<TokensBalance>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val activeTokensWithNetworks = db.getActiveTokensWithNetworks(masterWalletId)

                val grouped = activeTokensWithNetworks.groupBy {
                    it.network.name.lowercase()
                }

                val allBalances = enabledChains.map { chain ->
                    async {
                        val contractAddresses = grouped[chain.lowercase()]?.map { it.token.contractAddress }
                        val result = repository.getTokenBalance(walletAddress, chain, tokenAddress = contractAddresses)
                        result.result ?: emptyList()
                    }
                }.awaitAll().flatten()



                onResult(allBalances)
            } catch (e: Exception) {
                onResult(emptyList())
            }
        }
    }
    fun getAllAvailableTokensFromMoralisOnly(
        walletAddress: String,
        masterWalletId: String,
        enabledChains: List<String>,
        onResult: (List<TokensBalance>) -> Unit
    ) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                // Step 1: Load active toggled tokens
                val activeTokensWithNetworks = db.getActiveTokensWithNetworks(masterWalletId)

                Log.d("all tokens available" , " active Tokens with networks : $activeTokensWithNetworks")

                // Mapping chainId → Moralis chain string
                val chainMap = moralisChainMap

                Log.d("all tokens available" , "moralis chain map : $moralisChainMap")
                // Native special addresses
                val nativeAddressMap = mapOf(
                    "eth" to "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee",
                    "bsc" to "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee",
                    "polygon" to "0x0000000000000000000000000000000000001010"
                )

                // Group toggled tokens by Moralis chain
                val grouped = activeTokensWithNetworks.groupBy {
                    chainMap[it.network.chainId] ?: "unknown"
                }

                Log.d("all tokens available" , " grouped active tokens with networks : $grouped")

                // Step 2: Request token balances for all toggled tokens
                val balancesByChain = enabledChains.map { chain ->
                    async {
                        val tokensInChain = grouped[chain] ?: emptyList()
                        val contractAddresses = tokensInChain.map { it.token.contractAddress.lowercase() }.toMutableSet()

                        val nativeAddr = nativeAddressMap[chain]
                        val nativeToggled = nativeAddr != null && tokensInChain.any {
                            it.token.contractAddress.equals(nativeAddr, ignoreCase = true)
                        }

                        val balances = mutableListOf<TokensBalance>()

                        // 🔁 Fetch ERC-20 token balances
                        val erc20Contracts = contractAddresses.filterNot { it == nativeAddr }.toList()
                        if (erc20Contracts.isNotEmpty()) {
                            val result = repository.getTokenBalance(walletAddress, chain, erc20Contracts)
                            balances += (result.result ?: emptyList())
                        }

                        // 🔁 Fetch native token (if toggled) using null token list
                        if (nativeToggled) {
                            val result = repository.getTokenBalance(walletAddress, chain, null)
                            val native = result.result?.find {
                                it.tokenAddress.equals(nativeAddr, ignoreCase = true)
                            }
                            if (native != null) balances += native
                        }
                        chain to balances
                    }
                }.awaitAll().toMap()



                Log.d("all tokens available" , "balances by chain is : $balancesByChain")


                // Step 3: Extract tokens with balances
                val tokensWithBalance = balancesByChain.values.flatten()

                Log.d("all tokens available" , "tokens with balance : $tokensWithBalance")


                val tokensWithBalanceAddresses = tokensWithBalance.map { it.tokenAddress.lowercase() }.toSet()
                Log.d("all tokens available" , "tokens with balance addresses : $tokensWithBalanceAddresses")



                // Step 4: Extract toggled tokens that did NOT return from Moralis (0 balance)
                val zeroBalanceTokens = activeTokensWithNetworks.filter {
                    val contract = it.token.contractAddress.lowercase()
                    val chain = chainMap[it.network.chainId] ?: return@filter false
                    enabledChains.contains(chain) && !tokensWithBalanceAddresses.contains(contract)
                }

                Log.d("all tokens available" , "zero balances tokens : $zeroBalanceTokens")


                // Step 5: For those, fetch token price
                val tokenPriceRequestMap = zeroBalanceTokens.groupBy {
                    chainMap[it.network.chainId] ?: "unknown"
                }
                Log.d("all tokens available" , "token price request map  : $tokenPriceRequestMap")



                val zeroBalanceTokensWithPrices = tokenPriceRequestMap.flatMap { (chain, tokens) ->
                    val requestItems = tokens.map {
                        TokenPriceRequestItem(tokenAddress = it.token.contractAddress)
                    }


                    val prices = repository.getTokenPricesByContract(chain, requestItems)

                    Log.d("all tokens available" , "prices : $prices")


                    prices.map { price ->
                        TokensBalance(
                            tokenAddress = price.tokenAddress,
                            symbol = price.tokenSymbol,
                            name = price.tokenName,
                            logo = price.tokenLogo,
                            balance = "0",
                            balanceFormatted = "0",
                            usdPrice = (price.usdPrice ?: 0.0).toFloat(),
                            usdValue = 0.0f,
                            decimals = price.tokenDecimals.toIntOrNull() ?: 18,
                            nativeToken = nativeAddressMap[chain]?.equals(price.tokenAddress, ignoreCase = true) == true,
                            verifiedContract = price.verifiedContract,
                            possibleSpam = price.possibleSpam.toString(),
                            thumbnail = price.tokenLogo
                        )
                    }
                }

                Log.d("all tokens available" , "zero balance token with prices : $zeroBalanceTokensWithPrices")


                // Step 6: Merge & return
                val allTokensToDisplay = tokensWithBalance + zeroBalanceTokensWithPrices
                Log.d("all tokens available","all tokens are : $allTokensToDisplay")
                onResult(allTokensToDisplay)
                _uiState.value = UiState.Success(allTokensToDisplay)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(emptyList())
                _uiState.value = UiState.Error("something went wrong... $e")
            }
        }
    }


    suspend fun getMergedActivatedTokenBalances(
        walletAddress: String,
        networkName: String,
        masterWalletId: String
    ): NativesBalance {

        _uiState.value = UiState.Loading
        try {
            val activeTokens = db.getActiveTokensNow(masterWalletId)// collect first value

            val nativeBalances = repository.getTokenBalance(walletAddress, chain = networkName, tokenAddress = null)
            val nonZeroBalances = nativeBalances.result

            val balanceMap = nonZeroBalances.associateBy { it.tokenAddress.lowercase() }
            // 1. Use only tokens in activeTokens
            val mergedFromExternal = activeTokens.mapNotNull { token ->
                balanceMap[token.contractAddress.lowercase()]
            }
            // 2. Add fallback only for activeTokens not found in Moralis
            val fallbackFromLocal = activeTokens.filter {
                !balanceMap.containsKey(it.contractAddress.lowercase())
            }.map {
                balanceResponseFromLocalToken(it)
            }
            /*    val merged = activeTokens.map { token ->
                    val tokensBalance = balanceMap[token.contractAddress.lowercase()]
                    tokensBalance ?: balanceResponseFromLocalToken(token)
                }*/
            val merged = mergedFromExternal + fallbackFromLocal
            val nativeBalance = NativesBalance(
                cursor = "",
                page = 1,
                pageSize = merged.size,
                result = merged
            )
          //  _uiState.value = UiState.Success(nativeBalance)
            return nativeBalance
        } catch (e: IOException) {
            _uiState.value = UiState.Error("Failed To load tokens....")
            return NativesBalance(cursor = "", page = 1, pageSize = 0, result = emptyList())
        }
    }

    suspend fun getTokenBalancesByWallet(walletAddress: String, chain: String) {

            viewModelScope.launch {
                _uiState.value = UiState.Loading
                try {
                    val response = repository.getBalanceByWallet(address = walletAddress, chain)
                    Log.d("chains","the non-native Value is :  $response")
                } catch (e: Exception) {
                    Log.e("balance","balance error is $e")
                    _uiState.value = UiState.Error("an error is appear : $e")
                }
            }





    }

    fun getAllChainTokenBalances(
        walletAddress: String,
        masterWalletId: String,
        chains: List<String>,
        onResult: (List<Pair<String, TokensBalance>>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val allBalances = chains.map { chain ->
                    async {
                        val result =
                            getMergedActivatedTokenBalances(walletAddress, chain, masterWalletId)
                        result?.result?.map { token -> chain to token } ?: emptyList()
                    }
                }.awaitAll().flatten()


                onResult(allBalances)
            } catch (e: Exception) {
                onResult(emptyList()) // handle error gracefully
            }
        }
    }


    fun getAllTokens(masterWalletId: String?) {
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

    fun getNativeTokenPrice(chainId: Int) {
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

                val priceResponse = repository.getTokenPrice(tokenAddress, chain)
                _nativeTokenPriceUsd.value = priceResponse.usdPrice
                Log.d("TokenVM", "✅ Native token USD price: ${priceResponse.usdPrice}")
            } catch (e: Exception) {
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

    fun mapBalanceByWalletToTokensBalance(item: BalanceByWallet): TokensBalance {
        return TokensBalance(
            tokenAddress = item.tokenAddress,
            symbol = item.symbol,
            name = item.name,
            logo = item.logo,
            thumbnail = item.thumbnail,
            decimals = item.decimals,
            balance = item.balance,
            verifiedContract = item.verifiedContract,
            possibleSpam = item.possibleSpam.toString(), // Cast Boolean → String to match model
            totalSupply = item.totalSupply,
            totalSupplyFormated = item.totalSupplyFormated,
            percentageRelativeToTotalSupply = item.percentageRelativeToTotalSupply,
            securityScore = item.securityScore,
            balanceFormatted = item.balance, // fallback if no separate formatting
            usdPrice = 0f, // fallback
            usdPriceDayPercentChange = 0f,
            usdValue = 0f,
            usdValueDayUsdChange = 0f,
            nativeToken = false,
            portfolioPercentage = 0f
        )
    }

}

