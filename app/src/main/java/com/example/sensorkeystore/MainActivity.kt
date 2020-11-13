package com.example.sensorkeystore

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_pin.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var pair: Pair<ByteArray, ByteArray>

    private lateinit var cryptography: Cryptography

    private lateinit var ivPin: ImageView
    private lateinit var inputString: EditText
    private lateinit var ivFingerprint: ImageView
    private lateinit var btnSaveStringInKeyStore: Button
    private lateinit var btnGetLastSavedStringInKeystore: Button

    private lateinit var mBiometricManager: BiometricManager

    private var isFingerprintAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViewAndListeners()
        createPinIfNotExist()
        checkFingerprintIsAvailable()

        cryptography = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CryptographyAES()
        } else {
            CryptographyRSA(this)
        }
    }

    private fun initViewAndListeners() {
        ivPin = findViewById(R.id.iv_pin)
        inputString = findViewById(R.id.input_string)
        ivFingerprint = findViewById(R.id.iv_fingerprint)
        btnSaveStringInKeyStore = findViewById(R.id.btn_save_string_keystore)
        btnGetLastSavedStringInKeystore = findViewById(R.id.btn_get_last_saved_keystore)

        ivPin.setOnClickListener {
           showPinInputDialog()
        }

        ivFingerprint.setOnClickListener {
            callBiometric()
        }

        btnSaveStringInKeyStore.setOnClickListener {
            closeKeyboard()
            storeString()
        }

        btnGetLastSavedStringInKeystore.setOnClickListener {
            showAuthenticateMethods()
        }
    }

    private fun createPinIfNotExist() {
        if (getPin() == null)
            startActivity(Intent(this, CustomPinActivity::class.java))
    }

    private fun checkFingerprintIsAvailable() {
        mBiometricManager = BiometricManager.from(this)

        if (mBiometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            isFingerprintAvailable = true
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
            iv_fingerprint.visibility = View.GONE
            iv_pin.visibility = View.GONE

            val executor = ContextCompat.getMainExecutor(this)
            val callback = BiometricAuthenticationCallback(this, cryptography.decryptData(pair.first, pair.second))
            val biometricPrompt = BiometricPrompt(this, executor, callback)

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login")
                .setNegativeButtonText("Nagative button test")
                .build()

            biometricPrompt.authenticate(promptInfo)
    }

    private fun showPinInputDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_pin, null)

        val alertDialog = AlertDialog.Builder(this).setView(view).create()

        view.pin_view.setPinViewEventListener { pinview, _ ->

            iv_pin.visibility = View.GONE
            iv_fingerprint.visibility = View.GONE

            if (pinview.value == getPin()) {
                alertDialog.dismiss()
                showToast(cryptography.decryptData(pair.first, pair.second))
            } else {
                showToast("PIN is incorrect")
            }
        }

        alertDialog.show()
    }

    private fun showAuthenticateMethods() {
        if (this::pair.isInitialized) {
            if (isFingerprintAvailable) {
                iv_fingerprint.visibility = View.VISIBLE
                iv_pin.visibility = View.VISIBLE
            } else {
                iv_pin.visibility = View.VISIBLE
            }
        } else {
            showToast("You haven't store string in keystore")
        }

    }
}