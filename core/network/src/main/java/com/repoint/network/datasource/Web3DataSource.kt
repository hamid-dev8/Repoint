package com.repoint.network.datasource

import org.web3j.crypto.Credentials
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.TransactionManager
import java.math.BigDecimal
import java.math.BigInteger

interface Web3DataSource
{

    suspend fun testWeb3Connection() : Boolean
    suspend fun getChainId() : Long
    suspend fun getNonce(address : String) : BigInteger
    suspend fun getWalletBalance(walletAddress : String) : BigInteger
    suspend fun sendTokenOnChain(credentials: Credentials, amount : BigDecimal, recipientAddress : String) : TransactionReceipt?
    suspend fun sendNativeToken(credentials: Credentials,recipient : String,amount: BigDecimal) : EthSendTransaction
    suspend fun estimateGas(from : String,nonce : BigInteger,gasPrice : BigInteger,to : String,data : String) : BigInteger
    suspend fun getGasPrice() : BigInteger
}