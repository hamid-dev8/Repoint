package com.repoint.sources.datarepo

import android.util.Log
import com.repoint.database.dao.CmcTokenDao
import com.repoint.models.sharedmodels.local.LocalActiveNetworks
import com.repoint.models.sharedmodels.remote.TokenMetaData
import com.repoint.models.sharedmodels.rpc.AlchemyChain
import com.repoint.models.sharedmodels.rpc.AlchemyTokenBalance
import com.repoint.models.sharedmodels.rpc.AlchemyTokenBalanceResponse
import com.repoint.models.sharedmodels.rpc.FeeHistoryResult
import com.repoint.models.sharedmodels.rpc.GasPriceTier
import com.repoint.models.sharedmodels.ui.ApiException
import com.repoint.models.sharedmodels.ui.ApiResult
import com.repoint.network.di.Web3Provider
import com.repoint.network.util.resolveChainFromPlatform
import com.repoint.network.util.NetworkApiService
import com.repoint.network.util.safeApiCall
import com.repoint.sources.datarepo.datasource.AlchemyDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.protocol.core.DefaultBlockParameterName
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class AlchemyRepositoryImp @Inject constructor(private val networkApi: NetworkApiService,private val db : CmcTokenDao) :
    AlchemyDataSource {


    @Inject
    lateinit var web3Provider: Web3Provider

    override suspend fun getTokenBalances(walletAddress: String,contracts : List<String>): ApiResult<AlchemyTokenBalanceResponse> {
        return safeApiCall {

            Log.d("AlchemyDebug", "walletAddress: $walletAddress")

            val body = mapOf(
                "jsonrpc" to "2.0",
                "id" to 1,
                "method" to "alchemy_getTokenBalances",
                "params" to listOf(
                    walletAddress, contracts
                )
            )
            networkApi.getTokenBalances(body)
        }
    }

    override suspend fun getNativeBalanceForChain(walletAddress: String, chainId: Int): ApiResult<BigInteger> = withContext(Dispatchers.IO) {
        Log.d("Web3Chain", "🚀 Entering getNativeBalanceForChain for chainId=$chainId")

         safeApiCall {
            val web3 = try {
                web3Provider.getWeb3j(chainId).also {
                    Log.d("Web3Chain", "✅ Web3j instance created")
                }
            } catch (e: Exception) {
                Log.e("Web3Chain", "❌ Error creating Web3j for $chainId", e)
                throw e
            }

            val clientVersion = try {
                web3.web3ClientVersion().send().web3ClientVersion.also {
                    Log.d("Web3Chain", "👷 Connected to node: $it")
                }
            } catch (e: Exception) {
                Log.e("Web3Chain", "❌ Failed getting client version for $chainId", e)
                throw e
            }

            val balanceResp = try {
                web3.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST).send()
            } catch (e: Exception) {
                Log.e("Web3Chain", "❌ Failed fetching balance from node", e)
                throw e
            }

            val result = balanceResp.balance.also {
                Log.d("Web3Chain", "💰 Balance (wei) = $it")
            }

            result
        }
    }

    override suspend fun getMultiChainTokenBalances(
        walletAddress: String,
        tokenMetaMap: Map<String, TokenMetaData>,
        alchemyClientFactory: (AlchemyChain) -> NetworkApiService
    ): ApiResult<List<AlchemyTokenBalance>> = safeApiCall {

        val groupedByChain: Map<AlchemyChain, List<Pair<String, TokenMetaData>>> = tokenMetaMap
            .values
            .flatMap { meta ->
                meta.contractAddress.mapNotNull { contract ->
                    val chain = resolveChainFromPlatform(contract.platform.name) ?: return@mapNotNull null
                    chain to (contract.contractAddress.lowercase() to meta)
                }
            }
            .groupBy({ it.first }, { it.second })

        val finalBalances = mutableListOf<AlchemyTokenBalance>()

        groupedByChain.forEach { (chain, contracts) ->
            Log.d("AlchemyDebug", "Chain: $chain → ${contracts.size} contracts: $contracts")
        }
        for ((chain, tokenPairs) in groupedByChain) {
            val client = alchemyClientFactory(chain)
            val contractAddresses = tokenPairs.map { it.first }

            val requestBody = mapOf(
                "jsonrpc" to "2.0",
                "id" to 1,
                "method" to "alchemy_getTokenBalances",
                "params" to listOf(walletAddress, contractAddresses)
            )
            Log.d("AlchemyRequest", "Requesting $chain with ${contractAddresses.size} contracts → $contractAddresses")

            try {
                val result = client.getTokenBalances(requestBody)
                Log.d("AlchemyDebug", "Response from $chain → ${result.result.tokenBalances}")

                val balances = result.result.tokenBalances

                balances.forEachIndexed { index, rawBalance ->
                    val balanceHex = rawBalance.tokenBalance
                    val contractAddress = rawBalance.contractAddress.lowercase()
                    val matchedMeta = tokenPairs.find { (addr, _) ->
                        addr.equals(rawBalance.contractAddress, ignoreCase = true)
                    }?.second
                    val filtered = result.result.tokenBalances.filterNot {
                        it.tokenBalance == "0x0" || it.tokenBalance == "0x000...000"
                    }
                    Log.d("AlchemyDebug", "Filtered non-zero balances from $chain → ${filtered.size}")
                    val decimals = matchedMeta?.let { getTokenDecimals(it) }
                    val balance = balanceHex?.removePrefix("0x")?.toBigIntegerOrNull(16) ?: BigInteger.ZERO
                    val normalized = decimals?.let { balance.toBigDecimal().movePointLeft(it) }


                    finalBalances.add(
                        AlchemyTokenBalance(
                            contractAddress = rawBalance.contractAddress,
                            tokenBalance = normalized?.toPlainString(),
                            symbol = matchedMeta?.symbol!!,
                            name = matchedMeta.name.toString(),
                            logo = matchedMeta.logo.toString(),
                            chainSlug = chain.name.lowercase()
                        )
                    )
                    finalBalances.forEach{
                        Log.d("AlchemyParsed", "Parsed: ${it.name} (${it.symbol}) → ${it.tokenBalance}")
                    }
                }
            } catch (e: Exception) {
                Log.e("AlchemyChain", "Error fetching from ${chain.name}: ${e.message}")
            }
        }

        return@safeApiCall finalBalances
    }

    fun getTokenDecimals(meta: TokenMetaData): Int {
        return when (meta.symbol.uppercase()) {
            "USDT", "USDC" -> 6
            "DAI", "LINK", "POL" -> 18
            else -> 18
        }
    }


    override suspend fun getNativeBalance(walletAddress: String): ApiResult<String> {
        return safeApiCall {
            val body = mapOf(
                "jsonrpc" to "2.0",
                "id" to 1,
                "method" to "eth_getBalance",
                "params" to listOf(
                    walletAddress,"latest"
                )
            )
            val response = networkApi.getNativeBalance(body)
            response.result
        }
    }

    override suspend fun getFeeHistory(chainId: Long): ApiResult<FeeHistoryResult> {
        return safeApiCall {
            val client = networkApi
            val body = mapOf(
                "jsonrpc" to "2.0",
                "id" to 1,
                "method" to "eth_feeHistory",
                "params" to listOf(
                    "0x5",
                    "latest",
                    listOf(10,50,90)
                )
            )
            val response = client.getFeeHistory(body)
            response.result
        }
    }
    override suspend fun getGasPriceTiers(chainId: Long): ApiResult<GasPriceTier> {
        return when (val result = getFeeHistory(chainId)) {
            is ApiResult.Success -> {
                val baseFeeHex = result.data.baseFeePerGas.lastOrNull()
                val rewardHexes = result.data.reward.lastOrNull()

                if (baseFeeHex == null || rewardHexes == null || rewardHexes.size < 3) {
                    return ApiResult.Error(ApiException.Unauthorized())
                }

                val baseFee = baseFeeHex.removePrefix("0x").toBigIntegerOrNull(16) ?: BigInteger.ZERO
                val slow = rewardHexes[0].removePrefix("0x").toBigIntegerOrNull(16) ?: BigInteger.ZERO
                val average = rewardHexes[1].removePrefix("0x").toBigIntegerOrNull(16) ?: BigInteger.ZERO
                val fast = rewardHexes[2].removePrefix("0x").toBigIntegerOrNull(16) ?: BigInteger.ZERO

                fun toGwei(wei: BigInteger): BigDecimal =
                    wei.toBigDecimal().divide(BigDecimal("1000000000"))

                val tier = GasPriceTier(
                    slow = toGwei(baseFee + slow),
                    average = toGwei(baseFee + average),
                    fast = toGwei(baseFee + fast)
                )

                ApiResult.Success(tier)
            }

            is ApiResult.Error -> ApiResult.Error(result.exception)
        }
    }


    override suspend fun getTokenContracts(walletAddress: String): List<Int> {
        return db.getActiveTokenContracts(walletAddress)
    }

    override suspend fun debugAllActives(): List<LocalActiveNetworks> {
        return db.debugAllActives()
    }
}