package com.mtkreader.contracts

import com.mtkreader.data.DeviceDate
import com.mtkreader.data.DeviceTime

interface MonitorContract {

    interface View : BluetoothContract.View {
        fun displayTimeData(timeDate: Pair<String, String>)
        fun onTimeWriteResult(isSuccessful: Boolean)
    }

    interface Presenter : BluetoothContract.Presenter {
        fun stopTimeFetch()
        fun setTimeDate(time: DeviceTime, deviceDate: DeviceDate)
    }

    interface Service {
    }

}
