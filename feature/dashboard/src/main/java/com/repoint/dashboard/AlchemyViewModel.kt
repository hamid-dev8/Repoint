package com.repoint.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.basics.logic.chainIdFromSlug
import com.repoint.models.sharedmodels.local.LocalActiveNetworks
import com.repoint.models.sharedmodels.rpc.AlchemyChain
import com.repoint.models.sharedmodels.rpc.AlchemyTokenBalance
import com.repoint.models.sharedmodels.rpc.GasPriceTier
import com.repoint.models.sharedmodels.ui.ApiResult
import com.repoint.models.sharedmodels.ui.UiState
import com.repoint.network.di.AlchemyClientFactory
import com.repoint.sources.datarepo.datasource.AlchemyDataSource
import com.repoint.sources.datarepo.datasource.CmcDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class AlchemyViewModel @Inject constructor(
    private val alchemyRepository: AlchemyDataSource,
    private val cmcRepository: CmcDataSource,
) : ViewModel() {


    @Inject
    lateinit var alchemyClientFactory: AlchemyClientFactory
    private val _tokenBalances =
        MutableStateFlow<UiState<List<AlchemyTokenBalance>>>(UiState.Loading)
    val tokenBalances: StateFlow<UiState<List<AlchemyTokenBalance>>> = _tokenBalances


    private val _gasPriceTier = MutableStateFlow<UiState<GasPriceTier>>(UiState.Loading)
    val gasPriceTier: StateFlow<UiState<GasPriceTier>> = _gasPriceTier


    fun loadTokenBalances(masterWalletId: String, walletAddress: String) {
        viewModelScope.launch {
            Log.d("AlchemyVM", "Loading balances for wallet: $walletAddress")

            _tokenBalances.value = UiState.Loading
            // Log the contracts being queried
            val contracts = loadContractsList(masterWalletId)
            Log.d("AlchemyVM", "Querying contracts: $contracts")

            if (contracts.isEmpty()) {
                _tokenBalances.value = UiState.Success(emptyList())
                return@launch
            }

            val cmcInfoResult = cmcRepository.fetchTokenMetadata(ids = contracts)
            if (cmcInfoResult !is ApiResult.Success) {
                _tokenBalances.value = UiState.Error("Failed to fetch token metadata")
                return@launch
            }

            val tokenMetaMap = cmcInfoResult.data

            val result = alchemyRepository.getMultiChainTokenBalances(
                walletAddress = walletAddress,
                tokenMetaMap = tokenMetaMap.data,
                alchemyClientFactory = { chain -> alchemyClientFactory.getClient(chain) }
            )

            _tokenBalances.value = when (result) {
                is ApiResult.Success -> UiState.Success(result.data)
                is ApiResult.Error -> UiState.Error(result.exception.message ?: "Unknown error")
            }
            // Log.d("AlchemyRequest", "Sending balances request for $walletAddress with contracts = $tokenContracts")

            Log.d("Alchemy", "✅ Loaded balances for $walletAddress: ${_tokenBalances.value}")

        }
    }

    private val _nativeUsdPrice = MutableStateFlow<UiState<Double>>(UiState.Loading)
    val nativeUsdPrice: StateFlow<UiState<Double>> = _nativeUsdPrice

    fun loadNativeUsdPrice(chainId: Int) {
        viewModelScope.launch {
            _nativeUsdPrice.value = UiState.Loading
            val symbol = when (chainId) {
                1 -> "ETH"
                137 -> "POL"
                56 -> "BNB"
                else -> null
            } ?: return@launch

            when (val result = cmcRepository.getNativeTokenPriceBySymbol(symbol)) {
                is ApiResult.Success -> {
                    Log.d("CMC-USD", "Price for $symbol = ${result.data}")
                    _nativeUsdPrice.value = UiState.Success(result.data)
                }

                is ApiResult.Error -> {
                    Log.e("CMC-USD", "Failed to fetch price for $symbol: ${result.exception.message}")
                    _nativeUsdPrice.value =
                        UiState.Error(result.exception.message ?: "Failed")
                }
            }
        }
    }

    fun fetchGasPriceTiers(chainId: Long) {
        viewModelScope.launch {
            _gasPriceTier.value = UiState.Loading
            when (val result = alchemyRepository.getGasPriceTiers(chainId)) {
                is ApiResult.Success -> _gasPriceTier.value = UiState.Success(result.data)
                is ApiResult.Error -> _gasPriceTier.value =
                    UiState.Error(result.exception.message ?: "Unknown error")
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

    private val _nativeBalance = MutableStateFlow<UiState<BigDecimal>>(UiState.Loading)
    val nativeBalance: StateFlow<UiState<BigDecimal>> = _nativeBalance

    fun loadNativeBalance(walletAddress: String) {
        viewModelScope.launch {
            _nativeBalance.value = UiState.Loading
            when (val result = alchemyRepository.getNativeBalance(walletAddress)) {
                is ApiResult.Success -> {
                    Log.d("AlchemyCheck", "native result is $result")
                    val wei =
                        result.data.removePrefix("0x").toBigIntegerOrNull(16) ?: BigInteger.ZERO
                    Log.d("AlchemyCheck", "wei is  $wei")

                    val ether = wei.toBigDecimal().divide(BigDecimal("1000000000000000000"))
                    Log.d("AlchemyCheck", "ether result is $ether")

                    _nativeBalance.value = UiState.Success(ether)
                }

                is ApiResult.Error -> {
                    _nativeBalance.value =
                        UiState.Error(result.exception.message ?: "Unknown error")
                    Log.d("AlchemyCheck", "error of native result is ${result.exception.message}")

                }
            }
        }
    }

    private val _nativeBalanceByChain = MutableStateFlow<UiState<BigDecimal>>(UiState.Loading)
    val nativeBalanceByChain: StateFlow<UiState<BigDecimal>> = _nativeBalanceByChain

    fun loadNativeBalanceForChain(walletAddress: String , chainId : Int){
        Log.d("BalanceFlow", "Calling getNativeBalanceForChain on $walletAddress @ $chainId")

        viewModelScope.launch {
            _nativeBalanceByChain.value = UiState.Loading
            val result = alchemyRepository.getNativeBalanceForChain(walletAddress, chainId)
            when (result) {
                is ApiResult.Success -> {
                    val ether = result.data.toBigDecimal().divide(BigDecimal("1e18"))
                    _nativeBalanceByChain.value = UiState.Success(ether)
                }
                is ApiResult.Error -> {
                    _nativeBalanceByChain.value =
                        UiState.Error(result.exception.message ?: "Unknown error")
                }
            }


        }

    }

    fun reloadWalletData(onDone: () -> Unit,walletAddress: String,masterWalletId: String) {
        viewModelScope.launch {
            loadNativeBalance(walletAddress)
            loadTokenBalances(masterWalletId, walletAddress)
            delay(1000) // optional smooth delay
            onDone()
        }
    }

    private suspend fun loadContractsList(walletAddress: String): List<Int> {
        return alchemyRepository.getTokenContracts(walletAddress)
    }

    private suspend fun debugAllActives(): List<LocalActiveNetworks> {
        return alchemyRepository.debugAllActives()
    }

}