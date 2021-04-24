package com.mtkreader.contracts

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.mtkreader.commons.base.AutoDisposePresenter
import com.mtkreader.commons.base.ErrorHandlingFragment
import com.mtkreader.data.DataStructures
import com.mtkreader.data.SendData
import com.mtkreader.data.writing.DataRXMessage
import com.mtkreader.data.writing.DataTXMessage
import io.reactivex.Single

interface ParamsWriteContract {
    interface View : ErrorHandlingFragment {
        fun onReadyToConnect()
        fun onSocketConnected()
        fun onStatusUpdate(statusMessage: String)
        fun displayWaitMessage()
        fun onProgramingFinished(isSuccessful: Boolean)

        fun onError(throwable: Throwable)
    }

    interface Presenter : AutoDisposePresenter {
        fun extractFileData(fileLines: List<String>)
        fun connectToDevice(device: BluetoothDevice)
        fun stopTimeout()
        fun tryReset()
        fun closeConnection()
    }

    interface FillDataStructuresService {
        fun extractFileData(fileLines: List<String>): Single<DataStructures>
    }

    interface WriteDataService {
        fun generateStrings(data: DataStructures): Single<List<SendData>>
        fun createMessageObject(data: SendData): DataTXMessage
        fun createMessageObject(string: String): DataTXMessage
        fun createMTKCommandMessageObject(string: String): DataTXMessage
        fun isReadImageValid(dataRXMessage: DataRXMessage): Boolean
    }
}