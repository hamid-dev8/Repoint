package com.repoint.dashboard

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.repoint.splash.accountmanager.SpManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(private val spManager : SpManager) : ViewModel()
{

    val isScannerEnabled = mutableStateOf(false)
    val isPasscodeEnabled = mutableStateOf(false)
    val isTransactionSigningEnabled = mutableStateOf(false)

    init {
        //load saved values if using dataStore or preferences
    }

    fun toggleScanner(enabled : Boolean)
    {
        isScannerEnabled.value = enabled
    }

    fun togglePasscode(enabled : Boolean){
        isPasscodeEnabled.value = enabled
    }

    fun toggleTransactionSigning(enabled : Boolean){
        isTransactionSigningEnabled.value = enabled
    }



}