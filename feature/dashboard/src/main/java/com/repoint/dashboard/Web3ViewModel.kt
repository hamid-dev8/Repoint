package com.repoint.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.sources.datarepo.datasource.Web3DataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject


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


    init {
        fetchGasPrice()
    }

    suspend fun testConnectionToWeb3(): Boolean {
        return repository.testWeb3Connection()
    }

    suspend fun fetchNativeWalletBalance(walletAddress: String): BigDecimal? {

        return withContext(Dispatchers.IO) {
            try {
                val weiBalance = repository.getWalletBalance(walletAddress)
                // Convert wei to ether and return it
                Convert.fromWei(weiBalance.toString(), Convert.Unit.ETHER)
            } catch (ex: Exception) {
                ex.printStackTrace()
                BigDecimal.ZERO  // or throw the exception further
            }
        }
    }

    suspend fun sendTokenOnChain(
        credentials: Credentials,
        amount: BigDecimal,
        recipientAddress: String
    ): String? {
        try {
            val transactionReceipt =
                repository.sendTokenOnChain(credentials, amount, recipientAddress)
            Log.d(
                "transaction",
                "\uD83E\uDDE0 Sending Transaction : ${transactionReceipt?.transactionHash}"
            )
            if (transactionReceipt?.isStatusOK!!) {
                Log.d(
                    "transaction",
                    "✅ Transaction Successful: ${transactionReceipt.transactionHash}"
                )
                return transactionReceipt.transactionHash
            } else {
                Log.e("transaction", "❌ Transaction Failed!")
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
        amount: BigDecimal
    ): EthSendTransaction? {
        try {
            val ethResponse = repository.sendNativeToken(credentials, recipientAddress, amount)
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
            Log.d("transaction","failed with : $e")
            return null
        }
        return null
    }

    private fun fetchGasPrice() {
        viewModelScope.launch {
            try {
                val gasPriceWei = repository.getGasPrice()
                val gasPriceGwei = gasPriceWei.toBigDecimal()
                    .divide(BigDecimal.TEN.pow(9), 2, RoundingMode.HALF_UP)
                _gasPrice.value = gasPriceGwei // ✅ Convert Wei to Gwei
            } catch (e: Exception) {
                Log.d("transaction", "gas price error is : $e")
                _gasPrice.value = null
            }
        }
    }
}