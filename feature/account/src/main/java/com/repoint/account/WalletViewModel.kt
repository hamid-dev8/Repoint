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
import com.repoint.basics.logic.EcGenerator
import com.repoint.models.sharedmodels.local.ChainWallet
import com.repoint.models.sharedmodels.local.MasterWallet
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
/*
    private val _data = mutableStateOf("Nothing Yet!")
    val data: State<String> = _data
    val result = mutableStateOf<String?>(null)*/


        private val _walletCreated = mutableStateOf<String?>(null)
        val walletCreated : State<String?> = _walletCreated

    /*
    * Creates a new MasterWallet and derives ChainWallets for supported chains.
    * saves all Wallets into the database.
    * */

    fun createUserWallet(walletName : String = "Wallet",userId : String?)  {

        viewModelScope.launch {
            val (masterWallet , chainWallets) = EcGenerator.generateMasterAndChainWallets(walletName,userId)

            repository.insertMasterWallet(masterWallet)
            repository.insertChainWallets(chainWallets)

            _walletCreated.value = masterWallet.masterWalletId
            Log.d("WalletViewModel", "Wallet created: $masterWallet with ${chainWallets.size} chains")
            Log.d("WalletViewModel", "master wallet id is : ${masterWallet.masterWalletId} and actived in viewmodel")


            spManager.setActiveWallet(masterWallet.masterWalletId)
        }

    }

    fun importWallet(mnemonic : String,walletName : String) : String? {
        val isValid = EcGenerator.isValidMnemonic(mnemonic)

        if (!isValid) return null

        val (masterWallet , chainWallets) = EcGenerator.importMasterAndChainWallets(mnemonic,walletName)


        viewModelScope.launch {
            repository.insertMasterWallet(masterWallet)
            repository.insertChainWallets(chainWallets)
            Log.d("import", "Imported wallet: $masterWallet with ${chainWallets.size} chains")
            spManager.setActiveWallet(walletId = masterWallet.masterWalletId)
        }

        return masterWallet.masterWalletId
    }

    suspend fun getMasterWallet(masterWalletId: String) : MasterWallet{
        return repository.getMasterWallet(masterWalletId)
    }

    suspend fun getAllMasterWallets(userId: String) : List<MasterWallet> {
        return repository.getAllMasterWallets(userId)
    }

    suspend fun getAllChainWallets(masterWalletId : String) : List<ChainWallet>
    {
        return repository.getChainWalletsByMaster(masterWalletId)
    }

    fun renameChainWallet(chainWalletId : String , newName : String) {
        viewModelScope.launch {
            repository.renameChainWallet(chainWalletId,newName)
        }
    }

    fun deleteChainWallet(chainWalletId: String) {
        viewModelScope.launch {
            repository.deleteChainWallet(chainWalletId)
        }
    }

    suspend fun getChainWallet(masterWalletId : String, coinType : Int) : ChainWallet?{
        return repository.getChainWallet(masterWalletId,coinType)
    }

    fun linkUserToMasterWallet(masterWalletId: String, userId : String) {
        viewModelScope.launch {
            repository.linkMasterWalletToUser(masterWalletId,userId)
        }
    }



    }
    //val ecGen = EcGen.instance

   /* fun createUserWallet(): String? {


        val wallet = ecGen.getFromMnemonic()

        viewModelScope.launch {
            wallet.let { repository.authWallet(it) }
            Log.d(
                "viewww",
                "wallet id is : ${wallet.} ,user id is : ${wallet.userId}, wallet address is : ${wallet.privateKey} , wallet phrase is : ${wallet.phrase}"
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
            delay(20)
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
}*/