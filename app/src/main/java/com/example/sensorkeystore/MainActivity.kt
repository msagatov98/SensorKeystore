package com.example.sensorkeystore

import android.R.attr.end
import android.R.attr.start
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.security.auth.x500.X500Principal


class MainActivity : AppCompatActivity() {

    private val TRANSFORMATION =  "AES/CBC/PKCS7Padding"

    private lateinit var pair: Pair<ByteArray, ByteArray>

    private lateinit var inputString: EditText
    private lateinit var btnSaveStringInKeyStore: Button
    private lateinit var btnGetLastSavedStringInKeystore: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViewAndListeners()



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                "Key",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setUserAuthenticationRequired(true)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build()

            keyGenerator.init(keyGenParameterSpec)

            keyGenerator.generateKey()
        } else {

            val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")



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
            callBiometric()
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
                this, decryptData(
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
            pair = encryptData(inputString.text.toString())
            inputString.setText("")
            Toast.makeText(this, R.string.key_generate_success, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "You haven't store string in keystore", Toast.LENGTH_SHORT).show()
        }
    }

    private fun encryptData(data: String) : Pair<ByteArray, ByteArray> {

        val cipher = Cipher.getInstance(TRANSFORMATION)
        var temp = data

        while (temp.toCharArray().size % 16 != 0)
            temp += "\u0020"

        cipher.init(Cipher.ENCRYPT_MODE, getKey())

        val ivBytes = cipher.iv
        val encryptedBytes = cipher.doFinal(temp.toByteArray(Charsets.UTF_8))

        return Pair(ivBytes, encryptedBytes)
    }

    private fun decryptData(ivBytes: ByteArray, data: ByteArray): String {

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = IvParameterSpec(ivBytes)

        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)

        return cipher.doFinal(data).toString(Charsets.UTF_8).trim()
    }

    private fun getKey() : SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val secretKeyEntry = keyStore.getEntry("Key", null) as KeyStore.SecretKeyEntry
        return secretKeyEntry.secretKey
    }
}