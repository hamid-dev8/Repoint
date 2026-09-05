package com.repoint.dashboard

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.dependencies.accountmanager.SpManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LockMethod {
    PASSCODE,
    BIOMETRIC
}

enum class AutoLockOption(val minutes: Int, val label: String) {
    DISABLED(0, "Disabled"),
    ONE_MINUTE(1, "1 minute"),
    FIVE_MINUTES(5, "5 minutes"),
    FIFTEEN_MINUTES(15, "15 minutes");

    companion object {
        fun fromMinutes(minutes: Int): AutoLockOption {
           return values().firstOrNull { it.minutes == minutes } ?: DISABLED
        }
    }
}

@HiltViewModel
class SecurityViewModel @Inject constructor(private val spManager: SpManager) : ViewModel() {

    private val _isScannerEnabled = MutableStateFlow(false)
    val isScannerEnabled: StateFlow<Boolean> = _isScannerEnabled
    private val _isPasscodeEnabled = MutableStateFlow(false)
    val isPasscodeEnabled: StateFlow<Boolean> = _isPasscodeEnabled
    private val _lockMethod = MutableStateFlow(LockMethod.BIOMETRIC.name)
    val lockMethod: StateFlow<String> = _lockMethod
    private val _autoLockMinutes = MutableStateFlow(0)
    val autoLockMinutes: StateFlow<Int> = _autoLockMinutes
    private val _isTransactionSigningEnabled = MutableStateFlow(false)
    val isTransactionSigningEnabled: StateFlow<Boolean> = _isTransactionSigningEnabled

    // Becomes true only after the first real values have been read from
    // DataStore. Consumers (e.g. the launch-lock screen) must wait for this
    // before acting on lockMethod/isPasscodeEnabled, otherwise they'd act on
    // the transient default values above and briefly trigger the wrong
    // authenticator (e.g. flashing a biometric prompt before the saved
    // "Passcode" method loads).
    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded

    init {
        viewModelScope.launch {
           combine(
               spManager.getPasscodeEnabled(),
               spManager.getLockMethodFlow(),
               spManager.getAutoLockMinutesFlow(),
               spManager.getTransactionSigningEnabledFlow(),
               spManager.getScannerEnabledFlow()
           ) { passcodeEnabled, savedMethod, savedAutoLockMinutes, txSigningEnabled, scannerEnabled ->
               val resolvedMethod = savedMethod.takeIf { it.isNotBlank() }
                   ?: if (passcodeEnabled) LockMethod.PASSCODE.name else LockMethod.BIOMETRIC.name

               val resolvedPasscodeEnabled = resolvedMethod == LockMethod.PASSCODE.name || passcodeEnabled

               _lockMethod.value = resolvedMethod
               _isPasscodeEnabled.value = resolvedPasscodeEnabled
               _autoLockMinutes.value = savedAutoLockMinutes
               _isTransactionSigningEnabled.value = txSigningEnabled
               _isScannerEnabled.value = scannerEnabled
               _isLoaded.value = true
           }.collect { }
        }
    }

    fun toggleScanner(enabled: Boolean) {
        viewModelScope.launch {
           spManager.setScannerEnabled(enabled)
           _isScannerEnabled.value = enabled
        }
    }

    fun togglePasscode(enabled: Boolean) {
        viewModelScope.launch {
           val lockMethodValue = if (enabled) LockMethod.PASSCODE else LockMethod.BIOMETRIC
           spManager.setPasscodeEnabled(enabled)
           spManager.setLockMethod(lockMethodValue.name)
           _lockMethod.value = lockMethodValue.name
           _isPasscodeEnabled.value = enabled
        }
    }

    fun setLockMethod(method: LockMethod) {
        viewModelScope.launch {
           val enabled = method == LockMethod.PASSCODE
           spManager.setLockMethod(method.name)
           spManager.setPasscodeEnabled(enabled)
           _lockMethod.value = method.name
           _isPasscodeEnabled.value = enabled
        }
    }

    fun setAutoLockMinutes(minutes: Int) {
        viewModelScope.launch {
           spManager.setAutoLockMinutes(minutes)
           _autoLockMinutes.value = minutes
        }
    }

    fun toggleTransactionSigning(enabled: Boolean) {
        viewModelScope.launch {
            spManager.setTransactionSigningEnabled(enabled)
            _isTransactionSigningEnabled.value = enabled
        }
    }

    fun checkAndTriggerPasscode() {
        viewModelScope.launch {
           // val enabled = spManager.setPasscodeEnabled()
        }
    }

}