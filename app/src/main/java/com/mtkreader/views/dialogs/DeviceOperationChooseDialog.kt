package com.mtkreader.views.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.mtkreader.R
import kotlinx.android.synthetic.main.cant_find_device_dialog.*
import kotlinx.android.synthetic.main.device_operation_choose_dialog.*


class DeviceOperationChooseDialog(context: Context) :
    Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.device_operation_choose_dialog)
        initView()
    }

    private fun initView() {
        setCanceledOnTouchOutside(true)
        btn_read_time.setOnClickListener {  }
        btn_set_time.setOnClickListener { }
    }
}