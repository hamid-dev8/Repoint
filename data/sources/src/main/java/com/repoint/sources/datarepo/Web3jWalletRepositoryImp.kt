package com.repoint.sources.datarepo

import android.util.Log
import com.repoint.basics.logic.TokenERC20
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
class Web3jWalletRepositoryImp @Inject constructor(private val web3j: Web3j) :
    Web3DataSource {

    override suspend fun testWeb3Connection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
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

    override suspend fun getChainId(): Long = withContext(Dispatchers.IO) {
        return@withContext web3j.ethChainId().send().chainId.toLong()
    }

    override suspend fun getNonce(address: String): BigInteger = withContext(Dispatchers.IO) {
        return@withContext web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING)
            .send()
            .transactionCount
    }


    override suspend fun getWalletBalance(walletAddress: String): BigInteger =
        withContext(Dispatchers.IO) {
            web3j.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST)
                .sendAsync()
                .get()
                .balance
        }


    override suspend fun sendTokenOnChain(
        credentials: Credentials,
        amount: BigDecimal,
        recipientAddress: String
    ): TransactionReceipt? = withContext(Dispatchers.IO) {

        val senderAddress = credentials.address
        val chainId = getChainId()
        Log.d("transaction","the chain Id is : $chainId")
        val nonce = getNonce(senderAddress)
        val gasPrice = getGasPrice()

        val defaultGasLimit = BigInteger.valueOf(21_000)
        val gasLimit = estimateGas(senderAddress, nonce, gasPrice, "0xc2132D05D31c914a87C6611C10748AEb04B58e8F", encodeERC20Transfer(recipientAddress,amount))

        val erc20 = TokenERC20.load(
            "0xc2132D05D31c914a87C6611C10748AEb04B58e8F",
            web3j,
            credentials,
            gasPrice,
            gasLimit
        )
        val tokenDecimals = erc20.decimals().send().toInt()
        //val tokenAmount = amount.multiply(BigDecimal.TEN.pow(tokenDecimals)).toBigInteger()
        val amountInWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()
        Log.d("transaction", "🔹 Amount in Wei: $amountInWei")



        val rawTransaction = RawTransaction.createTransaction(
            nonce,
            gasPrice,
            gasLimit,
            "0xc2132D05D31c914a87C6611C10748AEb04B58e8F",
            encodeERC20Transfer(recipientAddress,amount)
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


        // ✅ Check if Transaction was Successful

        return@withContext transactionReceipt

    }

    override suspend fun sendNativeToken(
        credentials: Credentials,
        recipient: String,
        amount: BigDecimal
    ) : EthSendTransaction = withContext(Dispatchers.IO) {
        val senderAddress = credentials.address
        val gasLimit = BigInteger.valueOf(21_000)

        val amountInWei = Convert.toWei(amount,Convert.Unit.ETHER).toBigInteger()

        val transaction = RawTransaction.createEtherTransaction(
            getNonce(senderAddress),getGasPrice(),gasLimit,recipient,amountInWei
        )
        val signedMessage = TransactionEncoder.signMessage(transaction, credentials)
        val hexValue = Numeric.toHexString(signedMessage)
        // ✅ Send raw transaction
        val ethResponse = web3j.ethSendRawTransaction(hexValue).send()

        return@withContext ethResponse
    }

    override suspend fun estimateGas(
        from: String,
        nonce: BigInteger,
        gasPrice: BigInteger,
        to: String,
        data: String
    ): BigInteger =
        withContext(Dispatchers.IO) {
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
            Log.d("transaction","gas estimated is ${gasEstimate.amountUsed}")
            return@withContext gasEstimate.amountUsed
        }

    override suspend fun getGasPrice(): BigInteger = withContext(Dispatchers.IO) {
        val gasPriceWei = web3j.ethGasPrice().send().gasPrice
        gasPriceWei
    }

    fun encodeERC20Transfer(recipient : String,amount: BigDecimal) :String{
        val transferFunction = Function(
            "transfer",
            listOf(Address(recipient), Uint256(Convert.toWei(amount,Convert.Unit.ETHER).toBigInteger())),
            listOf(TypeReference.create(Bool::class.java))
        )
        Log.d("transaction","the encoded transfer function is ${FunctionEncoder.encode(transferFunction)}")
        return FunctionEncoder.encode(transferFunction)
    }
}