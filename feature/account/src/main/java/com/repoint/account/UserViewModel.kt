package com.repoint.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.basics.logic.generateSalt
import com.repoint.basics.logic.hashPasscode
import com.repoint.models.sharedmodels.User
import com.repoint.sources.datarepo.datasource.UserDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(private val repository : UserDataSource)  : ViewModel(){



    fun createUser(walletId : String,passcode : String) {

        viewModelScope.launch {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val createdAt = current.format(formatter)
            val salt = generateSalt()
            val hashed = hashPasscode(passcode,salt)
            val user = User(walletId = walletId, salt = salt, passwordHash = hashed,createdAt = createdAt)
            repository.authUser(user)
        }

    }
    suspend fun fetchUser() : User {

        val deferredUser = viewModelScope.async {
            val user = repository.getUser()
            user
        }

        return deferredUser.await()
    }


    override fun onCleared() {
        super.onCleared()
    }

}