package com.mtkreader.views.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.mtkreader.R
import kotlinx.android.synthetic.main.cant_find_device_dialog.*

class CantFindDeviceDialog(context: Context) :
    Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cant_find_device_dialog)
        initView()
    }

    private fun initView() {
        btn_dismiss.setOnClickListener { dismiss() }
    }
}