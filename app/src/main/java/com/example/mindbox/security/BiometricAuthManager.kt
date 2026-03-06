package com.example.mindbox.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.inject.Inject
import javax.inject.Singleton

sealed class BiometricResult {
    object Success : BiometricResult()
    data class Error(val code: Int, val message: String) : BiometricResult()
    object NotAvailable : BiometricResult()
    object NotEnrolled : BiometricResult()
}

@Singleton
class BiometricAuthManager @Inject constructor() {

    fun isAvailable(activity: FragmentActivity): Boolean {
        val manager = BiometricManager.from(activity)
        return manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun isEnrolled(activity: FragmentActivity): Boolean {
        val manager = BiometricManager.from(activity)
        return when (manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Unlock MindBox",
        subtitle: String = "Use your biometrics to access your memories",
        onResult: (BiometricResult) -> Unit
    ) {
        val manager = BiometricManager.from(activity)
        when (manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                onResult(BiometricResult.NotAvailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                onResult(BiometricResult.NotEnrolled)
                return
            }
            else -> Unit
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onResult(BiometricResult.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onResult(BiometricResult.Error(errorCode, errString.toString()))
            }

            override fun onAuthenticationFailed() {
                // Partial failure — prompt stays open, no callback needed
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }
}
