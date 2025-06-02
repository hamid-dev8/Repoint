package com.repoint.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.models.sharedmodels.remote.RepointTransactions
import com.repoint.sources.datarepo.datasource.HistoryDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HistoryViewModel @Inject constructor(private val repository: HistoryDataSource) :
    ViewModel() {


    private val _transactions = MutableLiveData<List<RepointTransactions>>()
    val transactions: LiveData<List<RepointTransactions>> = _transactions

    fun getAllChainHistory(address: String , chains : List<String> , order: String = "DESC") {
        viewModelScope.launch {
            try {
                val allTransactions = chains.map { chain ->

                    async {
                        fetchTransactionHistory(address,chain,order)
                    }
                }.awaitAll().flatten()

                _transactions.value = allTransactions.sortedByDescending { it.blockTimeStamp }
            }
            catch (e : Exception){
                _transactions.value = emptyList()
            }
        }
    }

     fun getNativeHistory(address: String, chain: String, order: String) {

        viewModelScope.launch {
            val history = fetchTransactionHistory(address, chain, order)
            _transactions.value = history

        }


    }


    private suspend fun fetchTransactionHistory(
        address: String,
        chain: String,
        order: String
    ): List<RepointTransactions> {
        return try {
            val response = repository.getNativeHistory(address, chain, order)
            response.transactions // Assuming the API response has a 'result' field
        } catch (e: Exception) {
            emptyList() // Handle errors gracefully
        }
    }

}