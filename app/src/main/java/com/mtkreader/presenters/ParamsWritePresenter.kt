package com.mtkreader.presenters

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.github.ivbaranov.rxbluetooth.BluetoothConnection
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.Const.BluetoothConstants.CONNECTION_TIMEOUT
import com.mtkreader.commons.base.BasePresenter
import com.mtkreader.contracts.ParamsWriteContract
import com.mtkreader.data.DataStructures
import com.mtkreader.data.SendData
import com.mtkreader.data.writing.DataRXMessage
import com.mtkreader.data.writing.DataTXMessage
import com.mtkreader.managers.DataManager
import com.mtkreader.utils.CommunicationUtil
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class ParamsWritePresenter(private val view: ParamsWriteContract.View) : BasePresenter(view),
    ParamsWriteContract.Presenter, KoinComponent {

    private val context: Context by inject()
    private val fillDataStructuresService: ParamsWriteContract.FillDataStructuresService by inject()
    private val writeDataService: ParamsWriteContract.WriteDataService by inject()
    private val bluetoothManager: RxBluetooth by inject()
    private lateinit var connection: BluetoothConnection
    private lateinit var socket: BluetoothSocket

    private lateinit var timeoutDisposable: Disposable
    private lateinit var initCommunicationDisposable: Disposable
    private lateinit var waitMessageDisposable: Disposable

    private val dataToWrite = mutableListOf<DataTXMessage>()
    private val dataManager = DataManager()


    private val statusObservable: PublishSubject<String> = PublishSubject.create()

    init {
        addDisposable(
            statusObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::onStatusUpdate, this::onErrorOccurred)
        )
    }

    override fun extractFileData(fileLines: List<String>) {
        addDisposable(
            fillDataStructuresService.extractFileData(fileLines)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onDataStructuresFilled, this::onErrorOccurred)
        )
    }

    private fun onDataStructuresFilled(fileData: DataStructures) {
        addDisposable(
            writeDataService.generateStrings(fileData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSendDataReady, this::onErrorOccurred)
        )
    }

    private fun onSendDataReady(sendData: List<SendData>) {
        dataToWrite.clear()
        dataToWrite.addAll(sendData.map { writeDataService.createMessageObject(it) })
        statusObservable.onNext("File parsed successfully!")
        view.onReadyToConnect()
    }

    override fun connectToDevice(device: BluetoothDevice) {
        addDisposable(
            bluetoothManager.connectAsClient(device, UUID.fromString(Const.BluetoothConstants.UUID))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSocketConnected, view::onError)
        )
        timeoutDisposable = Observable.timer(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                closeConnection()
                throw IOException("TimeOut!")
            }.doOnError { view.onError(it) }
            .subscribe()
        addDisposable(timeoutDisposable)
    }

    private fun onSocketConnected(socket: BluetoothSocket) {
        statusObservable.onNext("Connected")
        this.socket = socket
        view.onSocketConnected()
        startCommunication(socket)
        initDeviceCommunication()
    }

    private fun startCommunication(socket: BluetoothSocket) {
        statusObservable.onNext("Programming started!")

        connection = BluetoothConnection(socket)

        waitMessageDisposable = Observable.fromCallable { communicate() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onEndProgramming, this::onErrorOccurred)

        addDisposable(waitMessageDisposable)
        addDisposable(
            connection.observeByteStream()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::onReceiveByte, view::onError)
        )
    }

    private fun onReceiveByte(byte: Byte) {
        stopTimeout()
        dataManager.addData(byte)
    }

    private fun communicate(): DataRXMessage {
        val messages = mutableListOf<DataRXMessage>()
        val headerMessage = dataManager.waitForMessage()
        messages.add(headerMessage)

        CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.WRITE_PARAMS_SECOND_INIT)
        var message = dataManager.waitForMessage()
        if (message.buffer[1] != 'P'.toByte() || message.buffer[2] != '0'.toByte()) {
            CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.WRITE_PARAMS_THIRD_INIT)
            throw Exception(context.getString(R.string.set_in_programming_mode))
        }
        messages.add(message)
        for (data in dataToWrite) {
            val sendData = data.buffer.take(data.count)
            CommunicationUtil.writeToSocket(socket, sendData.toByteArray())
            message = dataManager.waitForMessage()
            messages.add(message)
        }

        val mtkMod = writeDataService.createMessageObject(SendData("E1", "0180", "", 0))
        CommunicationUtil.writeToSocket(socket, mtkMod.buffer.take(mtkMod.count).toByteArray())
        message = dataManager.waitForMessage()
        messages.add(message)

        // todo how in the fuck do i get this string
        //val waitMtkAnswer = writeDataService.createMessageObject("G6(A0A0A020)")
        val waitMtkAnswer = writeDataService.createMessageObject("G6(20202020)")
        CommunicationUtil.writeToSocket(socket, waitMtkAnswer.buffer.take(waitMtkAnswer.count).toByteArray())
        message = dataManager.waitForMessage()
        messages.add(message)

        val readProgramsCommand = writeDataService.createMTKCommandMessageObject("U")
        CommunicationUtil.writeToSocket(socket, readProgramsCommand.buffer.take(readProgramsCommand.count).toByteArray())
        return dataManager.waitForMessage(type = 1)
    }

    private fun onEndProgramming(readoutMessage: DataRXMessage) {
        statusObservable.onNext("Verifying...")
        addDisposable(Single.fromCallable { verifyReadout(readoutMessage) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::onProgramingFinished, this::onErrorOccurred)
        )
    }

    private fun verifyReadout(readoutMessage: DataRXMessage): Boolean {
        return writeDataService.isReadImageValid(readoutMessage)
    }

    private fun initDeviceCommunication() {
        statusObservable.onNext("Started communication!")
        initCommunicationDisposable =
            Observable.interval(Const.BluetoothConstants.INIT_COMMUNICATION_INTERVAL, TimeUnit.MILLISECONDS)
                .doOnNext {
                    CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.FIRST_INIT)
                    view.displayWaitMessage()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { this.onErrorOccurred(it) }
                .subscribe()
        addDisposable(initCommunicationDisposable)
    }

    override fun stopTimeout() {
        timeoutDisposable.dispose()
        initCommunicationDisposable.dispose()
    }

    override fun tryReset() {
        if (this::socket.isInitialized && socket.isConnected)
            CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.RESET)
    }

    override fun closeConnection() {
        if (this::connection.isInitialized)
            connection.closeConnection()
        clear()
    }


}