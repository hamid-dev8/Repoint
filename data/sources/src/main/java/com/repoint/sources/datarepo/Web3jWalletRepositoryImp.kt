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
        val web3j = web3Provider.getWeb3j(chainId = chainId.toInt())
        return@withContext web3j.ethChainId().send().chainId.toLong()
    }


    override suspend fun getNonce(address: String, chainId: Long): BigInteger =
        withContext(Dispatchers.IO) {
            val web3j = web3Provider.getWeb3j(chainId.toInt())
            return@withContext web3j.ethGetTransactionCount(
                address,
                DefaultBlockParameterName.PENDING
            )
                .send()
                .transactionCount
        }


    override suspend fun getWalletBalance(walletAddress: String, chainId: Long): BigInteger =
        withContext(Dispatchers.IO) {
            val web3j = web3Provider.getWeb3j(chainId.toInt())
            web3j.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST)
                .sendAsync()
                .get()
                .balance
        }


    override suspend fun sendTokenOnChain(
        credentials: Credentials,
        amount: BigDecimal,
        recipientAddress: String,
        contractAddress: String,
        networkChainId: Long
    ): TransactionReceipt? = withContext(Dispatchers.IO) {

        val senderAddress = credentials.address
        val chainId = networkChainId
        val web3j = web3Provider.getWeb3j(chainId = chainId.toInt())
        Log.d("transaction", "the chain Id is : $chainId")
        val nonce = getNonce(senderAddress, chainId)
        val gasPrice = getGasPrice(chainId)

        Log.d("transaction", "the gas price is  : $gasPrice")

        val tokenDecimals =
            getTokenDecimalsSafely(tokenAddress = contractAddress, credentials, chainId)

        val defaultGasLimit = BigInteger.valueOf(21_000)

        /*     val erc20 = TokenERC20.load(
                 contractAddress,
                 web3j,
                 credentials,
                 gasPrice,
                 gasLimit
             )*/
        // val tokenDecimals = erc20.decimals().send().toInt()
        //  val tokenAmount = amount.multiply(BigDecimal.TEN.pow(tokenDecimals)).toBigInteger()
        // val amountInWei = Convert.toWei(amount.movePointRight(tokenDecimals), Convert.Unit.ETHER)
        val amountInWei = amount.multiply(BigDecimal.TEN.pow(tokenDecimals)).toBigInteger()
        val data = encodeERC20Transfer(recipientAddress, amountInWei = amountInWei)
        val gasLimit = estimateGas(chainId, senderAddress, nonce, gasPrice, contractAddress, data)
        Log.d("transaction", "🔹 Amount in Wei: $amountInWei")


        val rawTransaction = RawTransaction.createTransaction(
            nonce,
            gasPrice,
            gasLimit,
            contractAddress,
            data
        )
        val transactionManager = RawTransactionManager(web3j, credentials, chainId)
        //Log.d("Transaction", "🔹 Encoded Function Call: $encodedFunction")

        val ethSendTransaction: EthSendTransaction =
            transactionManager.signAndSend(rawTransaction)

        val transactionHash = ethSendTransaction.transactionHash
        Log.d("Transaction", "🔹 Transaction Submitted! TX Hash: $transactionHash")

        var transactionReceipt: TransactionReceipt? = null

        var attempt = 0

        while (transactionReceipt == null && attempt < 20) {
            transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash)
                .send().transactionReceipt.orElse(null)
            if (transactionReceipt == null) {
                delay(2000)
                attempt++
            }
        }

        if (transactionReceipt == null) {
            Log.e("tx", "❌ TX was dropped or took too long")
        } else {
            Log.d("tx", "⛏️ Receipt status: ${transactionReceipt.status}")
        }
        // ✅ Check if Transaction was Successful

        return@withContext transactionReceipt

    }

    override suspend fun sendNativeToken(
        credentials: Credentials,
        recipient: String,
        amount: BigDecimal,
        networkChainId: Long
    ): EthSendTransaction = withContext(Dispatchers.IO) {
        val senderAddress = credentials.address
        val gasLimit = BigInteger.valueOf(21_000)

        Log.d("transaction", "Signing with chainId = $networkChainId")
        Log.d("transaction", "To: $recipient")

        val web3j = web3Provider.getWeb3j(networkChainId.toInt())
        val amountInWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()
        Log.d("transaction", "Amount (wei): $amountInWei")
        val nonce = getNonce(senderAddress, networkChainId)
        val gasPrice = getGasPrice(networkChainId)

        val transaction = RawTransaction.createEtherTransaction(
            nonce,
            gasPrice,
            gasLimit,
            recipient,
            amountInWei
        )

        val signedMessage = TransactionEncoder.signMessage(transaction, networkChainId,credentials)
        val hexValue = Numeric.toHexString(signedMessage)
        Log.d("transaction", "🚀 Raw TX Hex: $hexValue")
        // ✅ Send raw transaction
        val ethResponse = web3j.ethSendRawTransaction(hexValue).send()
        if (ethResponse.hasError()) {
            Log.e("transaction", "❌ Failed: ${ethResponse.error.message}")
        } else {
            Log.d("transaction", "✅ TX Sent: ${ethResponse.transactionHash}")
        }
        return@withContext ethResponse
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
            val web3j = web3Provider.getWeb3j(chainId.toInt())
            val gasEstimate = web3j.ethEstimateGas(
                Transaction.createFunctionCallTransaction(
                    from,
                    nonce,
                    gasPrice,
                    BigInteger.valueOf(100_000),
                    to,
                    BigInteger.ZERO,
                    data
                )
            ).send()
            if (gasEstimate.hasError()) {
                Log.e("gas", "❌ Gas estimation failed: ${gasEstimate.error.message}")
                throw Exception("Gas estimation failed: ${gasEstimate.error.message}")
            }

            val gasUsed = gasEstimate.amountUsed
            return@withContext gasUsed
        }

    override suspend fun getGasPrice(chainId: Long): BigInteger = withContext(Dispatchers.IO) {
        val web3j = web3Provider.getWeb3j(chainId.toInt())
        return@withContext web3j.ethGasPrice().send().gasPrice
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
            val gasLimit = BigInteger.valueOf(100_000)

            val token = TokenERC20.load(
                tokenAddress,
                web3j,
                credentials,
                gasPrice,
                gasLimit
            )

            val decimals = token.decimals().send()
            Log.d("token-decimals", "✔️ Decimals for $tokenAddress: $decimals")
            decimals.toInt()
        } catch (e: Exception) {
            Log.e(
                "token-decimals",
                "❌ Failed to get decimals for $tokenAddress. Defaulting to 18",
                e
            )
            18 // fallback default used by most tokens
        }
    }

}