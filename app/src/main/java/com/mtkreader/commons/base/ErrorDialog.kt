package com.mtkreader.commons.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.mtkreader.R
import kotlinx.android.synthetic.main.error_dialog.*

class ErrorDialog(
    context: Context,
    private val message: String = "",
    val onBack: () -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.error_dialog)
        window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        if (message.isNotEmpty())
            tv_error_title.text = message
        btn_back.setOnClickListener {
            dismiss()
            onBack()
        }
    }
}