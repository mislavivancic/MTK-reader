package com.mtkreader.presenters

import com.mtkreader.commons.Const
import com.mtkreader.commons.Const.Data.ACK
import com.mtkreader.commons.base.BaseBluetoothPresenter
import com.mtkreader.contracts.ParamsWriteContract
import com.mtkreader.data.DataStructures
import com.mtkreader.data.SendData
import com.mtkreader.data.writing.DataRXMessage
import com.mtkreader.data.writing.DataTXMessage
import com.mtkreader.exceptions.NotProgrammingModeException
import com.mtkreader.exceptions.ProgrammingError
import com.mtkreader.utils.CommunicationUtil
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.koin.core.KoinComponent
import org.koin.core.inject

class ParamsWritePresenter(private val view: ParamsWriteContract.View) : BaseBluetoothPresenter(view),
    ParamsWriteContract.Presenter, KoinComponent {

    private val fillDataStructuresService: ParamsWriteContract.FillDataStructuresService by inject()
    private val writeDataService: ParamsWriteContract.WriteDataService by inject()

    private lateinit var waitMessageDisposable: Disposable

    private val dataToWrite = mutableListOf<DataTXMessage>()

    private val statusObservable: PublishSubject<String> = PublishSubject.create()

    init {
        addDisposable(
            statusObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::onStatusUpdate, view::onError)
        )
    }

    override fun extractFileData(fileLines: List<String>) {
        addDisposable(
            fillDataStructuresService.extractFileData(fileLines)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onDataStructuresFilled, view::onError)
        )
    }

    private fun onDataStructuresFilled(fileData: DataStructures) {
        addDisposable(
            writeDataService.generateStrings(fileData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSendDataReady, view::onError)
        )
    }

    private fun onSendDataReady(sendData: List<SendData>) {
        dataToWrite.clear()
        dataToWrite.addAll(sendData.map { writeDataService.createMessageObject(it) })
        statusObservable.onNext("File parsed successfully!")
        view.onDataReady()
    }

    override fun startCommunication() {
        statusObservable.onNext("Programming started!")

        waitMessageDisposable = Observable.fromCallable { communicate() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onEndProgramming, view::onError)
        addDisposable(waitMessageDisposable)
        readStream()
        initDeviceCommunication()
    }

    override fun onByteReceive(byte: Byte) {
        stopTimeout()
        communicationManager.addData(byte)
    }

    private fun communicate(): DataRXMessage {
        val messages = mutableListOf<DataRXMessage>()
        val headerMessage = communicationManager.waitForMessage()
        messages.add(headerMessage)

        CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.WRITE_PARAMS_SECOND_INIT)
        var message = communicationManager.waitForMessage()
        if (message.buffer[1] != 'P'.toByte() || message.buffer[2] != '0'.toByte()) {
            CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.WRITE_PARAMS_THIRD_INIT)
            throw NotProgrammingModeException()
        }
        messages.add(message)
        for (data in dataToWrite) {
            val sendData = data.buffer.take(data.count)
            CommunicationUtil.writeToSocket(socket, sendData.toByteArray())
            message = communicationManager.waitForMessage()
            if (message.status != ACK) throw ProgrammingError()
            messages.add(message)
        }

        val mtkMod = writeDataService.createMessageObject(SendData("E1", "0180", "", 0))
        CommunicationUtil.writeToSocket(socket, mtkMod.buffer.take(mtkMod.count).toByteArray())
        message = communicationManager.waitForMessage()
        messages.add(message)

        // todo how in the fuck do i get this string
        //val waitMtkAnswer = writeDataService.createMessageObject("G6(A0A0A020)")
        val waitMtkAnswer = writeDataService.createMessageObject("G6(20202020)")
        CommunicationUtil.writeToSocket(socket, waitMtkAnswer.buffer.take(waitMtkAnswer.count).toByteArray())
        message = communicationManager.waitForMessage()
        messages.add(message)

        val readProgramsCommand = writeDataService.createMTKCommandMessageObject("U")
        CommunicationUtil.writeToSocket(socket, readProgramsCommand.buffer.take(readProgramsCommand.count).toByteArray())
        return communicationManager.waitForMessage(type = 1)
    }

    private fun onEndProgramming(readoutMessage: DataRXMessage) {
        statusObservable.onNext("Verifying...")
        addDisposable(Completable.fromCallable { verifyReadout(readoutMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::onProgramingFinished, view::onError)
        )
    }

    private fun verifyReadout(readoutMessage: DataRXMessage) {
        return writeDataService.isReadImageValid(readoutMessage)
    }

}
