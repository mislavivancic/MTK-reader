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
    private val onBackClickListener: View.OnClickListener? = null
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.error_dialog)
        window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        if (message.isNotEmpty())
            tv_error_title.text = message
        if (onBackClickListener != null)
            btn_back.setOnClickListener { onBackClickListener.onClick(it) }
        else
            btn_back.setOnClickListener { dismiss() }
    }
}