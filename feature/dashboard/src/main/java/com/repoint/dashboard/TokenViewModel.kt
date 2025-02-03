package com.repoint.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.models.sharedmodels.remote.NativesBalance
import com.repoint.network.datasource.TokenDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import javax.inject.Inject


@HiltViewModel
class TokenViewModel @Inject constructor(private val repository : TokenDataSource) : ViewModel()
{

    suspend fun getTokenBalance(address : String , chain : String) : NativesBalance {

       val tokenDeferred  =  viewModelScope.async {
            val result = repository.getTokenBalance(address,chain)
           Log.d("tokens","token balance result is : $result")
           result
        }

        return tokenDeferred.await()
    }


}