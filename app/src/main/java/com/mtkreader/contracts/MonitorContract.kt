package com.mtkreader.contracts

import com.mtkreader.data.DataStructMon

interface MonitorContract {

    interface View : BluetoothContract.View {
        fun onByte(byte: Byte)
        fun onDispStatus(s:String)
    }

    interface Presenter : BluetoothContract.Presenter {

    }

    interface Service {
        fun parseMonitor(str: String): DataStructMon
        fun SaveLogEvent()
    }

}
