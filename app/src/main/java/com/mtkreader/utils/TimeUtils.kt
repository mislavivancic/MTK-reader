package com.mtkreader.utils

import android.content.Context
import com.ikovac.timepickerwithseconds.MyTimePickerDialog
import com.mtkreader.R
import kotlinx.android.synthetic.main.fragment_connect.*
import java.util.*

object TimeUtils {

    fun provideTimePicker(
        context: Context,
        onTimeSetListener: MyTimePickerDialog.OnTimeSetListener
    ): MyTimePickerDialog {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        val second = c.get(Calendar.SECOND)
        return MyTimePickerDialog(context, onTimeSetListener, hour, minute, second, true).also {
            it.setMessage(context.getString(R.string.hour_min_sec))
        }
    }
}