package com.mtkreader.contracts

import com.mtkreader.data.DataStructures
import com.mtkreader.data.SendData
import com.mtkreader.data.writing.DataRXMessage
import com.mtkreader.data.writing.DataTXMessage
import io.reactivex.Single

interface ParamsWriteContract {

    interface View : BluetoothContract.View {
        fun onDataReady()
        fun onHtmlReady(html:String)
        fun onStatusUpdate(statusMessage: String)
        fun onProgramingFinished()
    }

    interface Presenter : BluetoothContract.Presenter {
        fun extractFileData(fileLines: List<String>)
        fun displayFileData(fileLines: List<String>)
    }

    interface FillDataStructuresService {
        fun extractFileData(fileLines: List<String>): Single<DataStructures>
    }

    interface WriteDataService {
        fun getVersions(header: ByteArray)
        fun generateStrings(data: DataStructures): List<SendData>
        fun setDataStructures(data: DataStructures)
        fun createMessageObject(data: SendData): DataTXMessage
        fun createMTKCommandMessageObject(string: String): DataTXMessage
        fun isReadImageValid(dataRXMessage: DataRXMessage)
    }
}
