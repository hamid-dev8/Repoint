package com.repoint.dashboard

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.dependencies.accountmanager.SpManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(private val spManager: SpManager) : ViewModel() {

    val isScannerEnabled = mutableStateOf(false)
    private val _isPasscodeEnabled = MutableStateFlow(false)
    val isPasscodeEnabled: StateFlow<Boolean> = _isPasscodeEnabled
    val isTransactionSigningEnabled = mutableStateOf(false)

    init {
        //load saved values if using dataStore or preferences
    }

    init {
        viewModelScope.launch {
            spManager.getPasscodeEnabled()
                .collect { enabled ->
                    _isPasscodeEnabled.value = enabled
                }
        }
    }

    fun toggleScanner(enabled: Boolean) {
        isScannerEnabled.value = enabled
    }

    fun togglePasscode(enabled: Boolean) {
        viewModelScope.launch {
            spManager.setPasscodeEnabled(enabled)
        }
    }

    fun toggleTransactionSigning(enabled: Boolean) {
        isTransactionSigningEnabled.value = enabled
    }

    fun checkAndTriggerPasscode() {
        viewModelScope.launch {
           // val enabled = spManager.setPasscodeEnabled()
        }
    }


}