package com.example.sensorkeystore

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment

class MainActivity : AppCompatActivity() {

    private lateinit var pair: Pair<ByteArray, ByteArray>

    private lateinit var cryptography: Cryptography

    private lateinit var inputString: EditText
    private lateinit var btnSaveStringInKeyStore: Button
    private lateinit var btnGetLastSavedStringInKeystore: Button

    private lateinit var mBiometricManager: BiometricManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViewAndListeners()

        mBiometricManager = BiometricManager.from(this)

        if (getPin() == null)
            startActivity(Intent(this, CustomPinActivity::class.java))



        cryptography = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CryptographyAES()
        } else {
            CryptographyRSA(this)
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
            //callBiometric()


        }
    }

    private fun closeKeyboard() {
        val view = this.currentFocus

        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun storeString() {
        if (inputString.text.isNotEmpty()) {
            try {
                pair = cryptography.encryptData(inputString.text.toString())
                inputString.setText("")
                Toast.makeText(this, R.string.key_generate_success, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(javaClass.simpleName, e.toString())
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            }
        } else {
            showToast("You haven't store string in keystore")
        }
    }

    private fun callBiometric() {
        if (this::pair.isInitialized) {
            val executor = ContextCompat.getMainExecutor(this)
            val callback = BiometricAuthenticationCallback(this, cryptography.decryptData(pair.first, pair.second))
            val biometricPrompt = BiometricPrompt(this, executor, callback)

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login")
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else
            showToast("You haven't store string in keystore")
    }
}