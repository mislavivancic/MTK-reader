package com.mtkreader.contracts

import android.content.Context
import com.mtkreader.data.DeviceDate
import com.mtkreader.data.DeviceTime
import com.mtkreader.data.writing.DataTXMessage

interface TimeContract {

    interface View : BluetoothContract.View {
        fun displayTimeData(timeDate: Pair<String, String>)
        fun onTimeWriteResult(isSuccessful: Boolean)
    }

    interface Presenter : BluetoothContract.Presenter {
        fun stopTimeFetch()
        fun setTimeDate(time: DeviceTime, deviceDate: DeviceDate)
    }

    interface Service {
        fun extractTimeData(context: Context, data: List<Char>, hardwareVersion: Int): Pair<String, String>
        fun generateTimeWriteMessage(time: DeviceTime, deviceDate: DeviceDate): DataTXMessage
        fun createTimeWriteMessage(time: String): DataTXMessage
    }

}