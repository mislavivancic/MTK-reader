package com.mtkreader.contracts

import android.bluetooth.BluetoothDevice
import com.mtkreader.commons.base.AutoDisposePresenter
import com.mtkreader.commons.base.ErrorHandlingFragment

interface BluetoothContract {


    interface View : ErrorHandlingFragment {
        fun onSocketConnected()

        fun displayWaitMessage(){}
        fun onError(throwable: Throwable)
    }


    interface Presenter:AutoDisposePresenter {
        fun connectToDevice(device: BluetoothDevice)
        fun startCommunication()

        fun stopTimeout()
        fun tryReset()
        fun closeConnection()
    }
}
