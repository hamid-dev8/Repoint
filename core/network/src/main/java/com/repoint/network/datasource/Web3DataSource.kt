package com.repoint.network.datasource

import java.math.BigInteger

interface Web3DataSource
{

    suspend fun getWalletBalance(walletAddress : String) : BigInteger

}