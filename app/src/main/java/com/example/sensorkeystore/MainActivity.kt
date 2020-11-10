package com.example.sensorkeystore

import android.app.KeyguardManager
import java.util.*
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import java.math.BigInteger
import java.security.KeyPair
import android.widget.Button
import android.content.Context
import android.widget.EditText
import javax.crypto.KeyGenerator
import java.security.KeyPairGenerator
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import android.security.KeyPairGeneratorSpec
import javax.security.auth.x500.X500Principal
import android.security.keystore.KeyProperties
import androidx.appcompat.app.AppCompatActivity
import android.view.inputmethod.InputMethodManager
import android.security.keystore.KeyGenParameterSpec


class MainActivity : AppCompatActivity() {

    private lateinit var pair: Pair<ByteArray, ByteArray>

    private lateinit var inputString: EditText
    private lateinit var btnSaveStringInKeyStore: Button
    private lateinit var btnGetLastSavedStringInKeystore: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViewAndListeners()

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (!keyguardManager.isKeyguardSecure)
            Toast.makeText(this, "Secure", Toast.LENGTH_SHORT).show()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                "Key",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build()

            keyGenerator.init(keyGenParameterSpec)

            keyGenerator.generateKey()
        } else {

            val startDate: Date = Calendar.getInstance().getTime()
            val endCalendar: Calendar = Calendar.getInstance()
            endCalendar.add(Calendar.YEAR, 1)
            val endDate: Date = endCalendar.getTime()
            val keyPairGeneratorSpec = KeyPairGeneratorSpec.Builder(this)
                .setAlias("Key")
                .setKeySize(4096)
                .setSubject(X500Principal("CN=Key"))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(startDate)
                .setEndDate(endDate)
                .build()

            val keyPairGenerator = KeyPairGenerator.getInstance(
                "RSA",
                "AndroidKeyStore"
            )
            keyPairGenerator.initialize(keyPairGeneratorSpec)

            keyPairGenerator.generateKeyPair()

        }


    }

    private fun initViewAndListeners() {
        inputString = findViewById(R.id.input_string)
        btnSaveStringInKeyStore = findViewById(R.id.btn_save_string_keystore)
        btnGetLastSavedStringInKeystore = findViewById(R.id.btn_get_last_saved_keystore)

        btnSaveStringInKeyStore.setOnClickListener {
            closeKeyboard()
            storeString()
        }

        btnGetLastSavedStringInKeystore.setOnClickListener {
            Toast.makeText(this, Cryptography.decryptData(pair.first, pair.second), Toast.LENGTH_SHORT).show()
        }
    }

    private fun closeKeyboard() {
        val view = this.currentFocus

        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun callBiometric() {
        if (this::pair.isInitialized) {
            val executor = ContextCompat.getMainExecutor(this)
            val callback = BiometricAuthenticationCallback(
                this, Cryptography.decryptData(
                    pair.first,
                    pair.second
                )
            )
            val biometricPrompt = BiometricPrompt(this, executor, callback)

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login to get data from keystore")
                .setNegativeButtonText("Use account password")
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else
            Toast.makeText(this, "You haven't store string in keystore", Toast.LENGTH_SHORT).show()
    }

    private fun storeString() {
        if (inputString.text.isNotEmpty()) {
            pair = Cryptography.encryptData(inputString.text.toString())
            inputString.setText("")
            Toast.makeText(this, R.string.key_generate_success, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "You haven't store string in keystore", Toast.LENGTH_SHORT).show()
        }
    }
}