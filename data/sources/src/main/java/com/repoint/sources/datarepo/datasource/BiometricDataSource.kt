package com.repoint.sources.datarepo.datasource

import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import androidx.fragment.app.FragmentActivity

interface BiometricDataSource
{

    fun isBiometricAvailable() : Boolean
    fun createBiometricPrompt(activity : FragmentActivity,callback: AuthenticationCallback) : BiometricPrompt
    fun createPromptInfo() : BiometricPrompt.PromptInfo

}