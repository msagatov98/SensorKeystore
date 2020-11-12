package com.example.sensorkeystore

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_pin.view.*


class DialogPin : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = layoutInflater.inflate(R.layout.dialog_pin, null)

        view.pin_view.setPinViewEventListener { pinview, fromUser ->
            requireContext().showToast("asdf")
        }

        return AlertDialog.Builder(activity).setView(view).create()

    }

}