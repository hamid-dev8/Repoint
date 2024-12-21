package com.repoint.account

import androidx.lifecycle.viewModelScope
import com.repoint.basics.viewmodel.BasicViewModel
import com.repoint.models.sharedmodels.UiState
import com.repoint.models.sharedmodels.User
import com.repoint.models.sharedmodels.Wallet
import com.repoint.sources.datarepo.UserRepositoryImp
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthViewModel @Inject constructor( private val repository : UserRepositoryImp) : BasicViewModel<UiState>()  {



    fun createUserWallet(user : User,wallet : Wallet){
        viewModelScope.launch {
            repository.authUser(user)
        }
    }




    override fun onCleared() {
        super.onCleared()
    }
}