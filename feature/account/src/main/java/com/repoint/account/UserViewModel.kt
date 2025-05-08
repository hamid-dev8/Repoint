package com.repoint.account

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.basics.logic.generateSalt
import com.repoint.basics.logic.hashPasscode
import com.repoint.models.sharedmodels.local.User
import com.repoint.sources.datarepo.datasource.UserDataSource
import com.repoint.dependencies.accountmanager.SpManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserDataSource,
    private val spManager: SpManager
) : ViewModel() {

    private val _result = MutableLiveData<User>()
    val result: LiveData<User> get() = _result

    fun createUser(passcode: String) {

        viewModelScope.launch {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val createdAt = current.format(formatter)
            val salt = generateSalt()
            val hashed = hashPasscode(passcode, salt)
            val user =
                User(salt = salt, passwordHash = hashed, createdAt = createdAt)

            spManager.setUserId(user.userId)
           repository.authUser(user)
            Log.d("focus" , " user have been created  : $user")
        }
    }

    suspend fun fetchUser(): User? {

            delay(1000)
            val user = repository.getUser()
            Log.d("focus" , "fetch user is : $user")


        return user
    }


    override fun onCleared() {
        super.onCleared()
    }

}