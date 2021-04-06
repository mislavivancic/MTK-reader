package com.mtkreader.contracts

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.fragment.app.Fragment
import com.mtkreader.commons.base.AutoDisposePresenter
import com.mtkreader.commons.base.ErrorHandlingFragment
import com.mtkreader.data.DeviceDate
import com.mtkreader.data.DeviceTime
import io.reactivex.Single

interface TimeContract {

    interface View : ErrorHandlingFragment {
        fun onSocketConnected(socket: BluetoothSocket)
        fun onReceiveBytes(byte: Byte)

        fun displayTimeData(time: String)
        fun onTimeWriteResult(isSuccessful: Boolean)

        fun onError(throwable: Throwable)
    }

    interface Presenter : AutoDisposePresenter {
        fun connectToDevice(device: BluetoothDevice)
        fun readStream(socket: BluetoothSocket)
        fun closeConnection()
        fun getTime()
        fun stopTimeFetch()
        fun extractTimeData(context: Context, data: List<Char>, hardwareVersion: Int)
        fun setTimeDate(time: DeviceTime, deviceDate: DeviceDate)
        fun setReadData(data: List<Char>)
    }

    interface Service {
        fun setSocket(socket: BluetoothSocket)
        fun extractTimeData(
            context: Context,
            data: List<Char>,
            hardwareVersion: Int
        ): Single<String>

        fun setTimeDate(time: DeviceTime, deviceDate: DeviceDate): Single<Boolean>
        fun setReadData(data: List<Char>)

    }

}