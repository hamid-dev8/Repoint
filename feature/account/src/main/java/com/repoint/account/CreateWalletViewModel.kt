package com.repoint.account

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.basics.logic.EcGen
import com.repoint.models.sharedmodels.RepointWallet
import com.repoint.sources.datarepo.datasource.AuthDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CreateWalletViewModel @Inject constructor(private val repository: AuthDataSource) :
    ViewModel() {

    private val _data = mutableStateOf("Nothing Yet!")
    val data: State<String> = _data
    val result = mutableStateOf<RepointWallet?>(null)

    fun createUserWallet() : String {
        val ecGen = EcGen()
        val wallet = ecGen.getFromMnemonic()
        viewModelScope.launch {
            repository.authWallet(wallet)
            Log.d(
                "viewww",
                "wallet id is : ${wallet.walletId} , wallet address is : ${wallet.privateKey} , wallet phrase is : ${wallet.phrase}"
            )
        }
        return wallet.phrase
    }

    fun showPhrase(id : String): List<String> {
        var phraseList: List<String>? = null

        viewModelScope.launch {
            //result.value = viewModel.showPhrase(viewModel.data.value).await()
            //phraseList = result.value!!.phrase.split(" ")
            phraseList = repository.getWallet(_data.value).phrase.split(" ")
        }

        return phraseList!!
    }

    override fun onCleared() {
        super.onCleared()
    }
}