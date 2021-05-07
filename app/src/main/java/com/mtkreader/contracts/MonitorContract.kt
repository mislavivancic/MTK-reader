package com.mtkreader.contracts

import com.mtkreader.data.DataStructMon

interface MonitorContract {

    interface View : BluetoothContract.View {
        fun onByte(byte: Byte)
        fun displayStatus(status: String)
        fun onStatusReadingInProgress()
        fun displayEventLog(eventLog: String)
        fun displayLearn(learn: String)
    }

    interface Presenter : BluetoothContract.Presenter {
        fun readEventLog()
        fun readLearn()
    }

    interface Service {
        fun parseMonitor(str: String): DataStructMon
        fun SaveLogEvent()
    }

}
