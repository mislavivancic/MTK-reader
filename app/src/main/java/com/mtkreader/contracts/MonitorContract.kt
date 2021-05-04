package com.mtkreader.contracts

import com.mtkreader.data.DataStructMon

interface MonitorContract {

    interface View : BluetoothContract.View {
        fun onByte(byte: Byte)

    }

    interface Presenter : BluetoothContract.Presenter {

    }

    interface Service {
        fun parseMonitor(str: String): DataStructMon
    }

}
