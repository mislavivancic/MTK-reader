package com.mtkreader.contracts

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.fragment.app.Fragment
import com.mtkreader.commons.base.AutoDisposePresenter
import com.mtkreader.commons.base.ErrorHandlingFragment

interface ReadingContract {

    interface View : ErrorHandlingFragment {
        fun provideFragment(): Fragment

        fun onSocketConnected(socket: BluetoothSocket)
        fun onReceiveBytes(byte: Byte)

        fun displayTimeData(time:String)

        fun onError(throwable: Throwable)
    }

    interface Presenter : AutoDisposePresenter {
        fun connectToDevice(device: BluetoothDevice)
        fun readStream(socket: BluetoothSocket)
        fun closeConnection()
        fun extractTimeData(context: Context, data: List<Char>, hardwareVersion: Int)
    }
}