package com.eon.futuresimulator.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat
import javax.inject.Inject
import javax.inject.Singleton

sealed interface BiometricResult {
    data object Success : BiometricResult
    data object NotAvailable : BiometricResult
    data class Failed(val message: String) : BiometricResult
}

/**
 * Gates the Privacy Center's sensitive actions (raw data Export, deleting all History)
 * behind BiometricPrompt when the user has enabled UserPreferencesRepository.biometricLockEnabled.
 */
@Singleton
class BiometricAuthHelper @Inject constructor() {

    fun canAuthenticate(activity: FragmentActivity): Boolean {
        val manager = BiometricManager.from(activity)
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(activity: FragmentActivity, title: String, subtitle: String, onResult: (BiometricResult) -> Unit) {
        if (!canAuthenticate(activity)) {
            onResult(BiometricResult.NotAvailable)
            return
        }
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onResult(BiometricResult.Success)
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onResult(BiometricResult.Failed(errString.toString()))
                }
            },
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
        prompt.authenticate(info)
    }
}
