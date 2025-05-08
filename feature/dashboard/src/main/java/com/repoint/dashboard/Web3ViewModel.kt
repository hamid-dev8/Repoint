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
import org.web3j.protocol.core.methods.response.TransactionReceipt
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
            /*viewModelScope.launch {
                val chainId = repository.getChainId()
                fetchGasPrice(chainId)
            }*/
        }


    suspend fun testConnectionToWeb3(chainId: Long): Boolean {
        return repository.testWeb3Connection(chainId)
    }

    suspend fun fetchNativeWalletBalance(walletAddress: String,chainId: Long): BigDecimal? {

        return withContext(Dispatchers.IO) {
            try {
                val weiBalance = repository.getWalletBalance(walletAddress, chainId = chainId)
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
        recipientAddress: String,
        contractAddress : String,
        networkChainId : Long
    ) : TransactionReceipt? {
        try {
            val transactionReceipt =
                repository.sendTokenOnChain(credentials, amount, recipientAddress = recipientAddress, contractAddress = contractAddress,networkChainId)
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
            }
            else{
                Log.d("transaction"," error in transit for address : $recipientAddress")
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
        chainId : Long
    ): EthSendTransaction? {
        try {
            val ethResponse = repository.sendNativeToken(credentials, recipientAddress, amount, networkChainId = chainId)
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

    suspend fun sendTokenDynamic(
        credentials: Credentials,
        amount: BigDecimal,
        recipientAddress: String,
        contractAddress: String?,
        chainId: Long
    ): String? = withContext(Dispatchers.IO) {


        return@withContext try {
            val txHash = if (isNativeToken(contractAddress)) {
                val tx = sendNativeToken(credentials, recipientAddress, amount, chainId)
                Log.d("tx", "✅ Native Token TX Sent: ${tx?.transactionHash}")
                tx?.transactionHash
            } else {
                val receipt = sendTokenOnChain(
                    credentials = credentials,
                    amount = amount,
                    recipientAddress = recipientAddress,
                    contractAddress = contractAddress!!,
                    networkChainId = chainId
                )
                receipt?.transactionHash.also {
                    Log.d("tx", "✅ ERC20 Token TX Sent: $it")
                }
            }
            txHash
        } catch (e: Exception) {
            Log.e("tx", "❌ Error sending token", e)
            null
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
}