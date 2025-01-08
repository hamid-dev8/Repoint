package com.repoint.account

import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.repoint.sources.datarepo.datasource.BiometricDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


@HiltViewModel
class BioViewModel @Inject constructor(
    private val biometricRepository: BiometricDataSource
) : ViewModel() {

    private val _isBiometricAvailable = MutableStateFlow(false)
    val isBiometricAvailable = _isBiometricAvailable.asStateFlow()


    fun checkBiometricAvailability() {
        _isBiometricAvailable.value = biometricRepository.isBiometricAvailable()
    }

    fun authenticate(activity: FragmentActivity,onSuccess : () -> Unit,onFailure : () -> Unit) {
        Log.d("auth","on fingerPrint")

        if (biometricRepository.isBiometricAvailable()) {
            val biometricPrompt = biometricRepository.createBiometricPrompt(
                activity,
                object : BiometricPrompt.AuthenticationCallback(){
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Log.d("auth","on success called $result")
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Log.d("auth","on error called : $errorCode + $errString")
                        onFailure()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Log.d("auth","on failed called")
                        onFailure()
                    }
                }
            )
            val promptInfo = biometricRepository.createPromptInfo()
            biometricPrompt.authenticate(promptInfo)
        }
    }

}