package com.example.textquest.data.security

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InternalBiometricManager(private val context: Context) {
    private val _status = MutableStateFlow(BiometricStatus.Idle)
    val status: StateFlow<BiometricStatus> = _status

    fun checkAvailability(): String {
        val manager = BiometricManager.from(context)
        return when (manager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Sensor Ready"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No Hardware"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Hardware Unavailable"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Not Enrolled"
            else -> "Unavailable"
        }
    }

    fun authenticate(activity: FragmentActivity, reason: String) {
        val manager = BiometricManager.from(context)
        val canAuth = manager.canAuthenticate(BIOMETRIC_STRONG)

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            _status.value = BiometricStatus.Failed
            Toast.makeText(context, "Sensor not ready: $canAuth", Toast.LENGTH_SHORT).show()
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                _status.value = BiometricStatus.Success
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                _status.value = BiometricStatus.Failed
                Toast.makeText(context, "Error: $errString", Toast.LENGTH_SHORT).show()
            }
            override fun onAuthenticationFailed() {
                _status.value = BiometricStatus.Failed
            }
        })

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authentication")
            .setSubtitle(reason)
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()

        _status.value = BiometricStatus.Authenticating
        prompt.authenticate(info)
    }

    fun reset() {
        _status.value = BiometricStatus.Idle
    }
}