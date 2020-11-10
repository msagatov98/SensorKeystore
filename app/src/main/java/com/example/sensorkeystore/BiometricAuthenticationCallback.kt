package com.example.sensorkeystore

import android.widget.Toast
import android.content.Context
import androidx.biometric.BiometricPrompt

class BiometricAuthenticationCallback(private val context: Context, private val text: String) : BiometricPrompt.AuthenticationCallback() {

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        Toast.makeText(context, "Authentication error: $errString", Toast.LENGTH_LONG).show()
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
    }

}