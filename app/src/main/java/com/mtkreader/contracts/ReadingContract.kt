package com.mtkreader.contracts

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import androidx.fragment.app.Fragment
import com.mtkreader.commons.base.AutoDisposePresenter
import com.mtkreader.commons.base.ErrorHandlingFragment

interface ReadingContract {

    interface View : ErrorHandlingFragment {
        fun provideFragment(): Fragment

        fun onSocketConnected(socket: BluetoothSocket)
        fun onReceiveBytes(byte: Byte)

        fun onError(throwable: Throwable)
    }

    interface Presenter : AutoDisposePresenter {
        fun connectToDevice(device: BluetoothDevice)
        fun readStream(socket: BluetoothSocket)
        fun closeConnection()
    }
}