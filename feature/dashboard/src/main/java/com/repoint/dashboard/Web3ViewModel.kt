package com.repoint.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.models.sharedmodels.rpc.AlchemyChainNativeBalance
import com.repoint.models.sharedmodels.ui.TxState
import com.repoint.models.sharedmodels.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Named


@HiltViewModel
class Web3ViewModel @Inject constructor(
    private val repository: com.repoint.sources.datarepo.datasource.Web3DataSource
) : ViewModel() {
    private val _balanceWei = MutableLiveData<BigInteger>()
    val balanceWei: LiveData<BigInteger> get() = _balanceWei

    //optionally expose the balance in ether
    private val _balanceEther = MutableLiveData<BigDecimal>()
    val balanceEther: LiveData<BigDecimal> get() = _balanceEther

    private val _gasPrice = MutableStateFlow<BigDecimal?>(null)
    val gasPrice: StateFlow<BigDecimal?> = _gasPrice

    private val _txState = MutableStateFlow<TxState>(TxState.Idle)
    val txState : StateFlow<TxState> get() = _txState

    private val _connectionStatus = MutableStateFlow<Boolean?>(null)
    val connectionStatus: StateFlow<Boolean?> get() = _connectionStatus



    init {
        /*viewModelScope.launch {
            val chainId = repository.getChainId()
            fetchGasPrice(chainId)
        }*/
    }


     fun testConnectionToWeb3(chainId: Long) {
        viewModelScope.launch {
            try {
                _connectionStatus.value = repository.testWeb3Connection(chainId)
            }
            catch (e : Exception){
                Log.e("Web3Connection", "❌ Connection test failed", e)
                _connectionStatus.value = false
            }
        }
    }

    suspend fun getChainId(chainId: Long): Long {
        return repository.getChainId(chainId)
    }

    fun fetchNativeWalletBalance(walletAddress: String, chainId: Long) {
        viewModelScope.launch {
            try {
                val balanceWei = repository.getWalletBalance(walletAddress, chainId)
                val balanceEther = Convert.fromWei(balanceWei.toBigDecimal(), Convert.Unit.ETHER)

                _balanceWei.postValue(balanceWei)
                _balanceEther.postValue(balanceEther)


                Log.d("Balance", "✅ $walletAddress → $balanceEther ETH")
            } catch (e: Exception) {
                Log.e("Balance", "❌ Failed to fetch balance for $walletAddress", e)
                // optionally post null or reset values
                _balanceWei.postValue(BigInteger.ZERO)
                _balanceEther.postValue(BigDecimal.ZERO)
            }
        }
    }


    suspend fun sendTokenOnChain(
        credentials: Credentials,
        amount: BigDecimal,
        recipientAddress: String,
        contractAddress: String,
        networkChainId: Long
    ): TransactionReceipt? {
        try {
            val transactionReceipt =
                repository.sendTokenOnChain(
                    credentials,
                    amount,
                    recipientAddress = recipientAddress,
                    contractAddress = contractAddress,
                    networkChainId
                )
            Log.d(
                "transaction",
                "\uD83E\uDDE0 Sending Transaction : ${transactionReceipt?.transactionHash}"
            )


            if (transactionReceipt == null) {
                Log.e("transaction", "❌ Transaction receipt polling failed or timed out")
            }
            if (transactionReceipt?.isStatusOK!!) {
                Log.d(
                    "transaction",
                    "✅ Transaction Successful: ${transactionReceipt.transactionHash}"
                )
                return transactionReceipt
            } else {
                Log.d("transaction", " error in transit for address : $recipientAddress")
                return null
            }
        } catch (e: Exception) {
            Log.e("transaction", "❌ Error Sending Tokens", e)
            return null
        }

    }

    suspend fun sendNativeToken(
        credentials: Credentials,
        recipientAddress: String,
        amount: BigDecimal,
        chainId: Long
    ): EthSendTransaction? {
        try {
            val ethResponse = repository.sendNativeToken(
                credentials,
                recipientAddress,
                amount,
                networkChainId = chainId
            )
            Log.d(
                "transaction",
                "\uD83E\uDDE0 Sending Transaction : ${ethResponse?.transactionHash}"
            )
            if (!ethResponse.hasError()) {
                Log.d(
                    "transaction",
                    "✅ Transaction Successful: ${ethResponse.transactionHash}"
                )
                return ethResponse
            } else {
                println("❌ Failed: ${ethResponse.error.message}")
            }
        } catch (e: Exception) {
            Log.d("transaction", "failed with : $e")
            return null
        }
        return null
    }

    suspend fun sendTokenDynamic(
        credentials: Credentials,
        amount: BigDecimal,
        recipientAddress: String,
        contractAddress: String?,
        chainId: Long
    ): String? = withContext(Dispatchers.IO) {
        _txState.value = TxState.Loading
        try {
            val txHash = if (isNativeToken(contractAddress)) {
                val tx = sendNativeToken(credentials, recipientAddress, amount, chainId)
                tx?.transactionHash
            }
            else {
                val receipt = sendTokenOnChain(
                    credentials = credentials,
                    amount = amount,
                    recipientAddress = recipientAddress,
                    contractAddress = contractAddress!!,
                    networkChainId = chainId
                )
                receipt?.transactionHash
            }

            if (txHash != null) {
                Log.d("tx", "✅ TX Success: $txHash")
                _txState.value = TxState.Success(txHash)
            } else {
                _txState.value = TxState.Error("Transaction failed or was dropped.")
            }

            return@withContext txHash
        }
        catch (e : Exception){
            Log.e("tx", "❌ TX Exception", e)
            _txState.value = TxState.Error("Exception during transaction", e)
            return@withContext null
        }
    }

    fun isNativeToken(contractAddress: String?): Boolean {
        return contractAddress.isNullOrBlank() || contractAddress.lowercase() in listOf(
            "0x0000000000000000000000000000000000000000", // Common native fallback
            "0x0000000000000000000000000000000000001010"  // Polygon native (POL) pseudo-address
        )
    }


    fun fetchGasPrice(chainId: Long) {
        viewModelScope.launch {
            try {
                val gasPriceWei = repository.getGasPrice(chainId = chainId)
                val gasPriceGwei = gasPriceWei.toBigDecimal()
                    .divide(BigDecimal.TEN.pow(9), 2, RoundingMode.HALF_UP)
                _gasPrice.value = gasPriceGwei // ✅ Convert Wei to Gwei
            } catch (e: Exception) {
                Log.d("transaction", "gas price error is : $e")
                _gasPrice.value = null
            }
        }
    }

    private val _allChainBalances = MutableStateFlow<UiState<List<AlchemyChainNativeBalance>>>(UiState.Loading)
    val allChainBalances: StateFlow<UiState<List<AlchemyChainNativeBalance>>> = _allChainBalances


    fun loadAllNativeBalances(walletAddress: String, chainMap: Map<Int, String>) {
        viewModelScope.launch {
            _allChainBalances.value = UiState.Loading

            val resultList = mutableListOf<AlchemyChainNativeBalance>()

            chainMap.entries.forEach { (chainId, chainName) ->
                try {
                    val wei = repository.getWalletBalance(walletAddress, chainId.toLong())
                    val ether = wei.toBigDecimal().divide(BigDecimal("1e18"))
                    Log.d("ChainBalance", " success on $chainName and the balance is : $ether")

                    resultList.add(AlchemyChainNativeBalance(chainId, chainName, ether))
                } catch (e: Exception) {
                    Log.e("ChainBalance", "❌ Failed on $chainName", e)
                    resultList.add(AlchemyChainNativeBalance(chainId, chainName, BigDecimal.ZERO))
                }
            }

            _allChainBalances.value = UiState.Success(resultList)
        }
    }



    fun formatNativeBalance(wei: BigInteger): BigDecimal {
        return wei.toBigDecimal().divide(BigDecimal("1e18"))
    }



}