package com.repoint.network

import com.repoint.network.datasource.Web3DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Web3jWalletRepositoryImp @Inject constructor(private val web3j: Web3j) : Web3DataSource {


    override suspend fun getWalletBalance(walletAddress: String) : BigInteger = withContext(Dispatchers.IO) {
        web3j.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST)
            .sendAsync()
            .get()
            .balance
    }


}