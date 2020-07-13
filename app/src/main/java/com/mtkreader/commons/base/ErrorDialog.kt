package com.mtkreader.commons.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.mtkreader.R
import kotlinx.android.synthetic.main.error_dialog.*

class ErrorDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.error_dialog)
        window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        btn_back.setOnClickListener { dismiss() }
    }
}