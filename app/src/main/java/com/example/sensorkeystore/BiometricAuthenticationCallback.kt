package com.example.sensorkeystore

import android.widget.Toast
import android.content.Context
import androidx.biometric.BiometricPrompt

class BiometricAuthenticationCallback(private val context: Context, private val text: String = "") : BiometricPrompt.AuthenticationCallback() {

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        context.showToast("Authentication error: $errString")
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        context.showToast(text)
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        context.showToast("Authentication failed")
    }

}