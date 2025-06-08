package com.repoint.sources.datarepo

import android.util.Log
import com.repoint.basics.logic.TokenERC20
import com.repoint.network.di.Web3Provider
import com.repoint.sources.datarepo.datasource.Web3DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.RawTransactionManager
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Web3jWalletRepositoryImp @Inject constructor(private val web3Provider: Web3Provider) :
    Web3DataSource {


    override suspend fun testWeb3Connection(chainId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val web3j = web3Provider.getWeb3j(chainId = chainId.toInt())
                val networkId = web3j.netVersion().send().netVersion
                val latestBlock = web3j.ethBlockNumber().send().blockNumber
                Log.d("Web3jTest", "Connected to Ethereum Network ID: $networkId")
                Log.d("Web3jTest", "Latest Block: $latestBlock")
                true
            } catch (e: Exception) {
                Log.e("Web3jTest", "Error connecting to Ethereum node", e)
                false
            }
        }
    }


    override suspend fun getChainId(chainId: Long): Long = withContext(Dispatchers.IO) {
        try {
            val web3j = web3Provider.getWeb3j(chainId = chainId.toInt())
            val actualChainId = web3j.ethChainId().send().chainId.toLong()

            Log.d("Web3ChainId", "✅ Chain ID from node: $actualChainId (expected: $chainId)")

            if (actualChainId != chainId) {
                Log.w(
                    "Web3ChainId",
                    "⚠️ Mismatch: requested chainId=$chainId but node reports $actualChainId"
                )
            }
            return@withContext actualChainId
        } catch (e: Exception) {
            Log.e("Web3ChainId", "❌ Failed to fetch chain ID for $chainId", e)
            throw e // or: return@withContext chainId (if you prefer fallback behavior)
        }
    }


    override suspend fun getNonce(address: String, chainId: Long): BigInteger =
        withContext(Dispatchers.IO) {
            try {
                val web3j = web3Provider.getWeb3j(chainId.toInt())
                val response = web3j.ethGetTransactionCount(
                    address,
                    DefaultBlockParameterName.PENDING
                ).send()

                val nonce = response.transactionCount
                Log.d("Web3Nonce", "✅ Nonce for $address on chainId $chainId: $nonce")
                return@withContext nonce
            } catch (e: Exception) {
                Log.e("Web3Nonce", "❌ Failed to get nonce for $address on chainId $chainId", e)
                throw e
            }
        }


    override suspend fun getWalletBalance(walletAddress: String, chainId: Long): BigInteger =
        withContext(Dispatchers.IO) {
            try {
                val web3j = web3Provider.getWeb3j(chainId.toInt())
                val response =
                    web3j.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST).send()

                val balance = response.balance

                Log.d(
                    "WalletBalance",
                    "✅ Balance for $walletAddress on chainId $chainId: $balance Wei"
                )
                return@withContext balance
            } catch (e: Exception) {
                Log.e(
                    "WalletBalance",
                    "❌ Failed to fetch balance for $walletAddress on chainId $chainId",
                    e
                )
                return@withContext BigInteger.ZERO
            }
        }


    override suspend fun sendTokenOnChain(
        credentials: Credentials,
        amount: BigDecimal,
        recipientAddress: String,
        contractAddress: String,
        networkChainId: Long
    ): TransactionReceipt? = withContext(Dispatchers.IO) {

        val web3j = web3Provider.getWeb3j(chainId = networkChainId.toInt())
        val senderAddress = credentials.address

        try {
            //step 1 : prepare values
            val nonce = getNonce(senderAddress, networkChainId)
            val gasPrice = getGasPrice(networkChainId)
            val tokenDecimals =
                getTokenDecimalsSafely(tokenAddress = contractAddress, credentials, networkChainId)
            val amountInWei = amount.multiply(BigDecimal.TEN.pow(tokenDecimals)).toBigInteger()

            Log.d("sendToken", "🧮 Decimals: $tokenDecimals | AmountInWei: $amountInWei")


            //step 2  : Encode ERC20 transfer
            val data = encodeERC20Transfer(recipientAddress, amountInWei = amountInWei)

            //step 3 : Estimate Gas
            val gasLimit = estimateGas(
                networkChainId,
                from = senderAddress,
                nonce = nonce,
                gasPrice = gasPrice,
                to = contractAddress,
                data = data
            )
            Log.d("sendToken", "⛽ GasPrice: $gasPrice | GasLimit: $gasLimit")

            val rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                contractAddress,
                data
            )
            val transactionManager = RawTransactionManager(web3j, credentials, networkChainId)

            val ethSendTransaction: EthSendTransaction =
                transactionManager.signAndSend(rawTransaction)
            val txHash = ethSendTransaction.transactionHash
            Log.d("sendToken", "🚀 TX Sent! Hash: $txHash")

            if (ethSendTransaction.hasError()) {
                Log.e("sendToken", "❌ TX Error: ${ethSendTransaction.error.message}")
                return@withContext null
            }
            var transactionReceipt: TransactionReceipt? = null

            repeat(20) {
                transactionReceipt =
                    web3j.ethGetTransactionReceipt(txHash).send().transactionReceipt.orElse(null)
                if (transactionReceipt != null) return@repeat
                delay(2000)
            }

            if (transactionReceipt == null) {
                Log.e("sendToken", "❌ TX dropped or timeout after 40s")
            } else {
                Log.d("sendToken", "✅ TX confirmed! Status: ${transactionReceipt!!.status}")
            }

            return@withContext transactionReceipt

        } catch (e: Exception) {
            Log.e("sendToken", "❌ Failed to send token TX", e)
            return@withContext null
        }
    }

    override suspend fun sendNativeToken(
        credentials: Credentials,
        recipient: String,
        amount: BigDecimal,
        networkChainId: Long
    ): EthSendTransaction = withContext(Dispatchers.IO) {
        val web3j = web3Provider.getWeb3j(networkChainId.toInt())
        val senderAddress = credentials.address

        try {
            val nonce = getNonce(senderAddress, networkChainId)
            val gasPrice = getGasPrice(networkChainId)
            val gasLimit = BigInteger.valueOf(21_000)
            val amountInWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()

            Log.d(
                "sendNative", """
            🔐 Sending Native Token:
            From: $senderAddress
            To:   $recipient
            Amount: $amountInWei wei
            Nonce: $nonce
            GasPrice: $gasPrice
            ChainId: $networkChainId
        """.trimIndent()
            )

            val rawTransaction = RawTransaction.createEtherTransaction(
                nonce,
                gasPrice,
                gasLimit,
                recipient,
                amountInWei
            )
            val signedMessage =
                TransactionEncoder.signMessage(rawTransaction, networkChainId, credentials)
            val hexValue = Numeric.toHexString(signedMessage)

            val ethResponse = web3j.ethSendRawTransaction(hexValue).send()
            if (ethResponse.hasError()) {
                Log.e("sendNative", "❌ Error sending TX: ${ethResponse.error.message}")
            } else {
                Log.d("sendNative", "✅ TX Hash: ${ethResponse.transactionHash}")
            }

            return@withContext ethResponse
        } catch (e: Exception) {
            Log.e("sendNative", "❌ Exception during native token send", e)
            throw e // or return an empty EthSendTransaction().withError()
        }
    }

    override suspend fun estimateGas(
        chainId: Long,
        from: String,
        nonce: BigInteger,
        gasPrice: BigInteger,
        to: String,
        data: String
    ): BigInteger =
        withContext(Dispatchers.IO) {
            try {
                val web3j = web3Provider.getWeb3j(chainId.toInt())
                val callTx = Transaction.createFunctionCallTransaction(
                    from,
                    nonce,
                    gasPrice,
                    null,
                    to,
                    BigInteger.ZERO, // value = 0 for ERC20 transfer
                    data
                )
                val response = web3j.ethEstimateGas(callTx).send()

                if (response.hasError()) {
                    Log.e("GasEstimation", "❌ Estimation error: ${response.error.message}")
                    throw Exception("Gas estimation failed: ${response.error.message}")
                }
                // Optional : add buffer (20%) to avoid understimation
                val estimated = response.amountUsed
                val buffered =
                    estimated.multiply(BigInteger.valueOf(120)).divide(BigInteger.valueOf(100))

                Log.d(
                    "GasEstimation", """
            ✅ Estimated gas: $estimated | Buffered: $buffered
            ➤ From: $from
            ➤ To: $to
            ➤ Data: ${data.take(20)}...
        """.trimIndent()
                )


                return@withContext buffered
            } catch (e: Exception) {
                Log.e("GasEstimation", "❌ Exception estimating gas", e)
                throw e
            }
        }

    override suspend fun getGasPrice(chainId: Long): BigInteger = withContext(Dispatchers.IO) {
        try {
            val web3j = web3Provider.getWeb3j(chainId.toInt())
            val response = web3j.ethGasPrice().send()

            if (response.hasError()) {
                Log.e("GasPrice", "❌ Error fetching gas price: ${response.error.message}")
                throw Exception("Gas price fetch failed: ${response.error.message}")
            }
            val gasPrice = response.gasPrice
            Log.d("GasPrice", "✅ Current gas price on chain $chainId: $gasPrice Wei")

            return@withContext gasPrice
        } catch (e: Exception) {
            Log.e("GasPrice", "❌ Exception while fetching gas price", e)
            throw e
        }
    }

    fun encodeERC20Transfer(recipient: String, amountInWei: BigInteger): String {
        val transferFunction = Function(
            "transfer",
            listOf(Address(recipient.lowercase()), Uint256(amountInWei)),
            listOf(TypeReference.create(Bool::class.java))
        )
        Log.d(
            "transaction",
            "the encoded transfer function is ${FunctionEncoder.encode(transferFunction)}"
        )
        return FunctionEncoder.encode(transferFunction)
    }


    suspend fun getTokenDecimalsSafely(
        tokenAddress: String,
        credentials: Credentials,
        chainId: Long
    ): Int = withContext(Dispatchers.IO) {
        try {
            val web3j = web3Provider.getWeb3j(chainId = chainId.toInt())
            val gasPrice = web3j.ethGasPrice().send().gasPrice
            val gasLimit = BigInteger.valueOf(100_000) //reasonable default for decimals...

            val erc20 = TokenERC20.load(
                tokenAddress,
                web3j,
                credentials,
                gasPrice,
                gasLimit
            )
            val decimals = erc20.decimals().send()
            Log.d("TokenDecimals", "✅ Token $tokenAddress reports decimals = $decimals")
            decimals.toInt()
        } catch (e: Exception) {
            Log.e("TokenDecimals", "❌ Failed to get decimals for token: $tokenAddress — defaulting to 18", e)
            18 // fallback // fallback default used by most tokens
        }
    }

}