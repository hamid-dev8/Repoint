package com.repoint.sources.datarepo.datasource

import org.web3j.crypto.Credentials
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.core.methods.response.TransactionReceipt
import java.math.BigDecimal
import java.math.BigInteger

interface Web3DataSource
{

    suspend fun testWeb3Connection(chainId: Long) : Boolean
     suspend fun getChainId(chainId: Long) : Long
    suspend fun getNonce(address : String,chainId: Long) : BigInteger
    suspend fun getWalletBalance(walletAddress : String,chainId: Long) : BigInteger
    suspend fun sendTokenOnChain(credentials: Credentials, amount : BigDecimal, recipientAddress : String,contractAddress : String,networkChainId : Long) : TransactionReceipt?
    suspend fun sendNativeToken(credentials: Credentials,recipient : String,amount: BigDecimal,networkChainId: Long) : EthSendTransaction
    suspend fun estimateGas(chainId: Long,from : String,nonce : BigInteger,gasPrice : BigInteger,to : String,data : String) : BigInteger
    suspend fun getGasPrice(chainId : Long) : BigInteger
}