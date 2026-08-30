package com.repoint.sources.datarepo

import android.util.Log
import com.repoint.models.sharedmodels.local.TxDirection
import com.repoint.models.sharedmodels.rpc.AlchemyTransferItem
import com.repoint.models.sharedmodels.rpc.RawContracts
import com.repoint.models.sharedmodels.rpc.TxHistoryItem
import com.repoint.models.sharedmodels.ui.ApiResult
import com.repoint.network.util.NetworkApiService
import com.repoint.network.util.safeApiCall
import com.repoint.sources.datarepo.datasource.AlchemyTxDataSource
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Named

class AlchemyTxRepositoryImp @Inject constructor(
    @Named("alchemy_eth") private val ethApi: NetworkApiService,
    @Named("alchemy_polygon") private val polygonApi: NetworkApiService,
    @Named("alchemy_bnb") private val bnbApi: NetworkApiService
) : AlchemyTxDataSource {
    override suspend fun getTransactionHistory(
        walletAddress: String,
        fromBlock: String?,
        toBlock: String?,
        pageKey: String?,
        maxCount: Int,
        category: List<String>,
        chainId: Int,
        direction: TxDirection
    ): ApiResult<Pair<List<TxHistoryItem>, String?>> {
        val api = when (chainId) {
            1 -> {
                Log.d("AlchemyRepo", "🧪 Using Ethereum Alchemy API")
                ethApi
            }
            137 -> {
                Log.d("AlchemyRepo", "🧪 Using Polygon Alchemy API")
                polygonApi
            }
            56 -> {
                Log.d("AlchemyRepo", "🧪 Using BNB Alchemy API")
                bnbApi
            }
            else -> {
                Log.w("AlchemyRepo", "⚠️ Unknown chainId $chainId, defaulting to Polygon")
                polygonApi
            }
        }

        return safeApiCall {
            val baseParams = mutableMapOf(
                "fromBlock" to (fromBlock ?: "0x0"),
                "toBlock" to (toBlock ?: "latest"),
                "maxCount" to "0x${maxCount.toString(16)}",
                "category" to category
            )

            val normalizedWallet = walletAddress.trim().lowercase()

            val calls = mutableListOf<Deferred<List<AlchemyTransferItem>>>()

            coroutineScope {
                if (direction != TxDirection.INCOMING) {
                    calls += async {
                        val params = baseParams.toMutableMap().apply {
                            put("fromAddress", walletAddress)
                            pageKey?.let { put("pageKey", it) }
                        }
                        val body = mapOf(
                            "jsonrpc" to "2.0",
                            "id" to 1,
                            "method" to "alchemy_getAssetTransfers",
                            "params" to listOf(params)
                        )
                        Log.d("AlchemyRepo", "📤 Outgoing tx request: $body")
                        api.getAssetTransfers(body).result.transfers
                    }
                }

                if (direction != TxDirection.OUTGOING) {
                    calls += async {
                        val params = baseParams.toMutableMap().apply {
                            put("toAddress", walletAddress)
                            pageKey?.let { put("pageKey", it) }
                        }
                        val body = mapOf(
                            "jsonrpc" to "2.0",
                            "id" to 1,
                            "method" to "alchemy_getAssetTransfers",
                            "params" to listOf(params)
                        )
                        Log.d("AlchemyRepo", "📤 Incoming tx request: $body")
                        api.getAssetTransfers(body).result.transfers
                    }
                }
            }

            val allTransfers = calls.awaitAll().flatten()

            Log.d("AlchemyRepo", "📥 Received ${allTransfers.size} transfers from chain $chainId")

            val txs = allTransfers.map {
                Log.d("AlchemyRepo", "↪️ Comparing it.to=${it.to} vs wallet=$walletAddress")

                TxHistoryItem(
                    hash = it.hash,
                    from = it.from,
                    to = it.to,
                    value = it.value ?: "0",
                    asset = sanitizeAssetName(it.asset),
                    timestamp = it.metaData?.blockTimestamp ?: "unknown",
                    isIncoming = it.to.trim().lowercase() == normalizedWallet,
                    chainSlug = when (chainId) {
                        1 -> "eth"
                        137 -> "polygon"
                        56 -> "bnb"
                        else -> "unknown"
                    },
                    blockNum = it.blockNum.toString(),
                    category = it.category.toString(),
                    rawContract = it.rawContract?.let { raw ->
                        RawContracts(
                            value = raw.value.toString(),
                            address = raw.address.toString(),
                            decimal = raw.decimal.toString()
                        )
                    }
                )
            }.distinctBy { it.hash + it.timestamp }
                .sortedWith(
                    compareByDescending<TxHistoryItem> { parseTxTimestamp(it.timestamp) }
                        .thenByDescending { it.blockNum.toLongOrNull() ?: 0L }
                )

            txs to null // pageKey support optional here
        }
    }

}

private fun parseTxTimestamp(value: String): Long {
    if (value.isBlank() || value.equals("unknown", ignoreCase = true)) return 0L

    return try {
        value.toLong()
    } catch (_: NumberFormatException) {
        try {
            Instant.parse(value).epochSecond
        } catch (_: DateTimeParseException) {
            try {
                LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .atZone(ZoneId.systemDefault())
                    .toEpochSecond()
            } catch (_: DateTimeParseException) {
                0L
            }
        }
    }
}

private fun sanitizeAssetName(asset: String?): String {
    val cleaned = asset?.trim().orEmpty()
    return if (cleaned.length > 15 || cleaned.contains("www.") || cleaned.contains("http")) {
        cleaned.take(10) + "..." // 🧹 Prevent UI breakage from scam strings
    } else cleaned
}