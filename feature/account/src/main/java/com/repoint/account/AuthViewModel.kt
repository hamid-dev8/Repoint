package com.repoint.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.database.dao.AuthDao
import com.repoint.models.sharedmodels.User
import com.repoint.models.sharedmodels.Wallet
import com.repoint.sources.datarepo.datasource.AuthDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor( private val repository : AuthDataSource) : ViewModel()  {


    fun createUserWallet(user : User,wallet : Wallet){
        viewModelScope.launch {
            repository.authUser(user)
        }
    }




    override fun onCleared() {
        super.onCleared()
    }
}