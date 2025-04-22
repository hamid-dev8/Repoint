package com.repoint.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.models.sharedmodels.local.TokenEntity
import com.repoint.models.sharedmodels.remote.NativesBalance
import com.repoint.models.sharedmodels.remote.TokensBalance
import com.repoint.sources.datarepo.datasource.NetworkDataSource
import com.repoint.sources.datarepo.datasource.TokenDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log


@HiltViewModel
class TokenViewModel @Inject constructor(private val repository : TokenDataSource,private val db : NetworkDataSource) : ViewModel()
{


    suspend fun getMergedActivatedTokenBalances(walletAddress: String,networkName : String,masterWalletId : String) : NativesBalance {

        val activeTokens = db.getActiveTokensNow(masterWalletId)// collect first value

        val nonZeroBalances = repository.getTokenBalance(walletAddress, chain = networkName).result

        val balanceMap = nonZeroBalances?.associateBy { it.tokenAddress.lowercase() }
        val merged = activeTokens.map { token ->
            val tokensBalance = balanceMap?.get(token.contractAddress.lowercase())
            tokensBalance ?: balanceResponseFromLocalToken(token)
        }

        return NativesBalance(
            cursor = "",
            page = 1,
            pageSize = merged.size,
            result = merged
        )
    }


/*
    suspend fun getActivatedTokens() : List<Int>{
        return db.getActiveTokens().map { it.tokenId }
    }
*/

    fun balanceResponseFromLocalToken(token : TokenEntity) : TokensBalance{
        return TokensBalance(
            tokenAddress = token.contractAddress,
            symbol = token.symbol,
            name = token.name,
            logo = token.logoUrl,
            decimals = token.decimals,
            thumbnail = token.logoUrl,
            balance = "0"
        )
    }

}

