package com.repoint.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.models.sharedmodels.local.TxDirection
import com.repoint.models.sharedmodels.rpc.TxHistoryItem
import com.repoint.models.sharedmodels.ui.ApiResult
import com.repoint.sources.datarepo.datasource.AlchemyTxDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject


@HiltViewModel
class TxHistoryViewModel @Inject constructor(
    private val repository : AlchemyTxDataSource
) : ViewModel()
{

    private val _txHistory = MutableStateFlow<List<TxHistoryItem>>(emptyList())
    val txHistory : StateFlow<List<TxHistoryItem>> = _txHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var nextPageKey : String? = null
    private var isFetchingMore = false


    fun loadAllChains(walletAddress: String) {
        val normalizedAddress = walletAddress.trim().lowercase()

        Log.d("TxHistoryVM", "🟡 Starting to load transactions for: $walletAddress")
        _isLoading.value = true

        viewModelScope.launch {
            val chains = listOf(1, 137, 56)
            val allTxs = mutableListOf<TxHistoryItem>()

            chains.map { chainId ->
                async {
                    Log.d("TxHistoryVM", "🔄 Fetching tx history from chain $chainId")
                    val result = repository.getTransactionHistory(
                        walletAddress = walletAddress,
                        fromBlock = "0x0",
                        toBlock = "latest",
                        pageKey = null,
                        maxCount = 100,
                        category = listOf("external", "erc20", "internal", "erc721"),
                        chainId = chainId,
                        direction = TxDirection.BOTH
                    )
                    Log.d("TxHistoryVM", "✅ Received result from chain $chainId: ${result is ApiResult.Success}")
                    result
                }
            }.awaitAll().forEach { result ->
                if (result is ApiResult.Success) {
                    Log.d("TxHistoryVM", "📦 Adding ${result.data.first.size} txs from one chain")
                    allTxs.addAll(result.data.first)
                } else if (result is ApiResult.Error) {
                    Log.e("TxHistoryVM", "❌ Error fetching history: ${result.exception.message}")
                }
            }

            _txHistory.value = allTxs.sortedWith(
                compareByDescending<TxHistoryItem> { parseTxTimestamp(it.timestamp) }
                    .thenByDescending { it.blockNum.toLongOrNull() ?: 0L }
            )
            Log.d("TxHistoryVM", "🟢 Final merged tx count: ${_txHistory.value.size}")
            _isLoading.value = false
        }
    }

    fun loadMore(walletAddress: String, chainId: Int) {
        if (isFetchingMore || nextPageKey == null) return
        isFetchingMore = true
        viewModelScope.launch {
            when (val result = repository.getTransactionHistory(walletAddress, pageKey = nextPageKey, chainId = chainId, direction = TxDirection.BOTH)) {
                is ApiResult.Success -> {
                    val (newTxs, newPageKey) = result.data
                    _txHistory.value = (_txHistory.value + newTxs).sortedWith(
                        compareByDescending<TxHistoryItem> { parseTxTimestamp(it.timestamp) }
                            .thenByDescending { it.blockNum.toLongOrNull() ?: 0L }
                    )
                    nextPageKey = newPageKey
                }
                is ApiResult.Error -> { }
            }
            isFetchingMore = false
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
                    LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).atZone(ZoneId.systemDefault()).toEpochSecond()
                } catch (_: DateTimeParseException) {
                    0L
                }
            }
        }
    }

}