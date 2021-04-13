package com.mtkreader.contracts

import com.mtkreader.commons.base.AutoDisposePresenter
import com.mtkreader.commons.base.ErrorHandlingFragment
import io.reactivex.Completable

interface ParamsWriteContract {
    interface View : ErrorHandlingFragment {
        // fun onSocketConnected(socket: BluetoothSocket)
        // fun onReceiveBytes(byte: Byte)

        fun onError(throwable: Throwable)
    }

    interface Presenter : AutoDisposePresenter {
        fun extractFileData(fileLines: List<String>)
        // fun connectToDevice(device: BluetoothDevice)
        // fun readStream(socket: BluetoothSocket)
        // fun closeConnection()
    }

    interface Service {
        fun extractFileData(fileLines: List<String>): Completable
    }
}