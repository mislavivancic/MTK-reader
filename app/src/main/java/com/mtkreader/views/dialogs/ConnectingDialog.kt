package com.mtkreader.views.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.mtkreader.R

class ConnectingDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connecting_dialog)
        window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }
}