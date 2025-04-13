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
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log


@HiltViewModel
class TokenViewModel @Inject constructor(private val repository : TokenDataSource,private val db : NetworkDataSource) : ViewModel()
{

    suspend fun getTokenBalance(address : String , chain : String) : NativesBalance {

       val tokenDeferred  =  viewModelScope.async {
            val result = repository.getTokenBalance(address,chain)
           //modified
            val activeTokensIds = db.getActiveTokens().map { it.contractAddress.lowercase() }
           val filteredTokens = result.result?.filter {
               it.tokenAddress.lowercase() in activeTokensIds
           }
           //
           Log.d("tokens","token balance result is : $result")
           //result
           NativesBalance(result = filteredTokens)
        }

        return tokenDeferred.await()
    }


    fun getAllActivatedTokenBalances(walletAddress: String, chain: String): List<TokensBalance> {
        var mergedTokens: List<TokensBalance> = emptyList()

        viewModelScope.launch {
            try {
                // 1. Get list of active token IDs from local DB
                val activeTokens = db.getActiveTokens() // Should return List<TokenEntity>

                // 2. Fetch actual token balances from Moralis
                val moralisTokens = repository.getTokenBalance(walletAddress, chain) // Should return List<MoralisToken>

                // 3. Merge logic: for every active token, check if it exists in Moralis result
                mergedTokens = activeTokens.map { token ->
                    val matchingToken = moralisTokens.result?.find { it.tokenAddress.equals(token.contractAddress, ignoreCase = true) }
                    if (matchingToken != null) {
                        matchingToken
                    } else {
                        TokensBalance(
                            tokenAddress = token.contractAddress,
                            name = token.name,
                            symbol = token.symbol,
                            logo = token.logoUrl,
                            balance = "0",
                            decimals = token.decimals,
                            thumbnail = token.logoUrl
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("TokenViewModel", "Error merging token balances", e)
            }
        }

        return mergedTokens
    }


    suspend fun getMergedActivatedTokenBalances(walletAddress: String,networkName : String) : NativesBalance? {
        //Fetch active tokens from local db
        val activeTokens = db.getActiveTokens()

        val nonZeroBalances = repository.getTokenBalance(walletAddress, chain = networkName).result

        val balanceMap = nonZeroBalances?.associateBy { it.tokenAddress.lowercase() }

        val merged = activeTokens.map { token ->
            val contractAddress = token.contractAddress.lowercase()
            val tokensBalance = balanceMap?.get(contractAddress)

            tokensBalance ?: TokensBalance(
                tokenAddress = token.contractAddress,
                symbol = token.symbol,
                name = token.name,
                logo = token.logoUrl,
                balance = "0",
                balanceFormatted = "0",
                usdPrice = 0.0f,
                usdValue = 0.0f,
                decimals = token.decimals,
                nativeToken = false,
                verifiedContract = true,
                possibleSpam = "false",
                thumbnail = "null"
            )
        }

        return NativesBalance(
            cursor = "",
            page = 1,
            pageSize = merged.size,
            result = merged
        )
    }


    suspend fun getActivatedTokens() : List<Int>{
        return db.getActiveTokens().map { it.tokenId }
    }

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