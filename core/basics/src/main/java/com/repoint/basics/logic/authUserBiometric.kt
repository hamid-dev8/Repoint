package com.repoint.basics.logic

import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

fun authenticateUser(onSuccess: () -> Unit,activity: FragmentActivity) {

    val biometricManager = BiometricManager.from(activity)
    if (biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_CANCELED
                ) {
                    // user cancelled or tapped cancel
                    activity.finishAffinity()
                } else {
                    // System already shows error in bottom sheet
                    Log.d("Biometric", "Error: $errString") // Optional
                }
            }

            override fun onAuthenticationFailed() {
                // ❗ DO NOT finish the app — let Android handle UI feedback
                // This triggers "Fingerprint not recognized" in bottom sheet
                Log.d("Biometric", "Authentication failed - fingerprint mismatch")
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("RePoint Login")
            .setSubtitle("Authenticate to continue")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    } else {
        onSuccess() // fallback if biometric not available
    }
}