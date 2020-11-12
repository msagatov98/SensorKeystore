package com.example.sensorkeystore

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import com.goodiebag.pinview.Pinview

class CustomPinActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_pin)
        
        val pinView = findViewById<Pinview>(R.id.pin_view)
        
        pinView.setPinViewEventListener { pinview, fromUser ->
            closeKeyboard()
            savePin(pinview.value)
            finish()
        }
    }

    private fun closeKeyboard() {
        val view = this.currentFocus

        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}