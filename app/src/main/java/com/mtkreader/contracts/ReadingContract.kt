package com.mtkreader.contracts

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.fragment.app.Fragment
import com.mtkreader.commons.base.AutoDisposePresenter
import com.mtkreader.commons.base.ErrorHandlingFragment
import com.mtkreader.data.DeviceDate
import com.mtkreader.data.DeviceTime
import io.reactivex.subjects.PublishSubject

interface ReadingContract {

    interface View : ErrorHandlingFragment {
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