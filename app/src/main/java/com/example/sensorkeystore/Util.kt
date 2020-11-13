package com.example.sensorkeystore

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(stringRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, stringRes, duration).show()
}

fun Context.savePin(pin: String) {
    val sp = getSharedPreferences("SP", MODE_PRIVATE)
    val editor = sp.edit()

    editor.putString("PIN", pin)
    editor.apply()
}

fun Context.getPin() : String? {
    val sp = getSharedPreferences("SP", MODE_PRIVATE)
    return sp.getString("PIN", null)
}