package com.mtkreader.contracts

import com.mtkreader.commons.base.AutoDisposePresenter
import com.mtkreader.commons.base.ErrorHandlingFragment
import com.mtkreader.data.DataStructures
import io.reactivex.Single

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

    interface FillDataStructuresService {
        fun extractFileData(fileLines: List<String>): Single<DataStructures>
    }

    interface WriteDataService {
        fun generateStrings(data: DataStructures): Single<String>
    }
}