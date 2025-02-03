package com.repoint.account

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewModelScope
import com.repoint.basics.logic.EcGen
import com.repoint.models.sharedmodels.local.RepointWallet
import com.repoint.sources.datarepo.datasource.AuthDataSource
import com.repoint.splash.accountmanager.SpManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: AuthDataSource,
    private val spManager: SpManager
) :
    ViewModel() {

    private val _data = mutableStateOf("Nothing Yet!")
    val data: State<String> = _data
    val result = mutableStateOf<String?>(null)
    val ecGen = EcGen.instance

    fun createUserWallet(): String? {


        val wallet = ecGen.getFromMnemonic()

        viewModelScope.launch {
            wallet.let { repository.authWallet(it) }
            Log.d(
                "viewww",
                "wallet id is : ${wallet.walletId} ,user id is : ${wallet.userId}, wallet address is : ${wallet.privateKey} , wallet phrase is : ${wallet.phrase}"
            )
        }
        return wallet.walletId
    }
    //return wallet.phrase    }

    suspend fun showPhrase(walletId : String): RepointWallet {
        val deferredWallet = viewModelScope.async {
            val wallet = repository.getWallet(walletId)
            Log.d("showPh", walletId.toString())
            wallet
        }

        return deferredWallet.await()
    }

    suspend fun getAllWallets(userId : String) : List<RepointWallet>{

        val deferredWallets = viewModelScope.async {
            delay(2000)
            val wallets = repository.getAllWallets(userId)
            wallets
        }

        return deferredWallets.await()
    }

     fun importWallet(mnemonic: String, walletName: String): String? {
        val wallet = ecGen.importWithMnemonic(mnemonic, walletName = walletName)

        if (wallet != null) {
            viewModelScope.launch {
                Log.d("import", "wallet is = $wallet")
                wallet.let { repository.authWallet(it) }
            }
        }

        return wallet?.walletId
    }

     fun linkUserToWallet(walletId : String, userId : String) {

        viewModelScope.launch {
            repository.linkWalletToUser(walletId,userId)
        }

    }


    override fun onCleared() {
        super.onCleared()
    }
}