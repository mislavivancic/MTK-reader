package com.mtkreader.contracts

import com.mtkreader.data.DataStructures
import com.mtkreader.data.SendData
import com.mtkreader.data.writing.DataRXMessage
import com.mtkreader.data.writing.DataTXMessage
import io.reactivex.Single

interface ParamsWriteContract {

    interface View : BluetoothContract.View {
        fun onDataReady()
        fun onStatusUpdate(statusMessage: String)
        fun onProgramingFinished(isSuccessful: Boolean)
    }

    interface Presenter : BluetoothContract.Presenter {
        fun extractFileData(fileLines: List<String>)
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