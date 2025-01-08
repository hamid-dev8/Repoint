package com.repoint.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.models.sharedmodels.User
import com.repoint.sources.datarepo.datasource.UserDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(private val repository : UserDataSource)  : ViewModel(){



    fun createUser(user : User) {
        viewModelScope.launch {
            repository.authUser(user)
        }

    }
    suspend fun fetchUser(id : Int) : User {

        val deferredUser = viewModelScope.async {
            val user = repository.getUser(id)
            user
        }

        return deferredUser.await()
    }


    override fun onCleared() {
        super.onCleared()
    }

}