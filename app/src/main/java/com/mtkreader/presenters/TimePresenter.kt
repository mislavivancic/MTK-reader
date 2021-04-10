package com.mtkreader.presenters

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.github.ivbaranov.rxbluetooth.BluetoothConnection
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BasePresenter
import com.mtkreader.contracts.TimeContract
import com.mtkreader.data.DeviceDate
import com.mtkreader.data.DeviceTime
import com.mtkreader.utils.CommunicationUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class TimePresenter(private val view: TimeContract.View) : BasePresenter(view),
    TimeContract.Presenter, KoinComponent {

    companion object {
        private const val INIT_COMMUNICATION_INTERVAL: Long = 2000
        private const val TIME_QUERY_INTERVAL: Long = 2000
        private const val CONNECTION_TIMEOUT: Long = 10000
    }

    private val bluetoothManager: RxBluetooth by inject()
    private val service: TimeContract.Service by inject()
    private lateinit var connection: BluetoothConnection
    private lateinit var socket: BluetoothSocket
    private val data = mutableListOf<Char>()
    private lateinit var getTimeDisposable: Disposable
    private lateinit var timeoutDisposable: Disposable
    private lateinit var initCommunicationDisposable: Disposable

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

    override fun initDeviceCommunication() {
        initCommunicationDisposable =
            Observable.interval(INIT_COMMUNICATION_INTERVAL, TimeUnit.MILLISECONDS)
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

    private fun onSocketConnected(socket: BluetoothSocket) {
        this.socket = socket
        service.setSocket(socket)
        view.onSocketConnected(socket)
    }

    override fun readStream(socket: BluetoothSocket) {
        connection = BluetoothConnection(socket)

        addDisposable(
            connection.observeByteStream()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(view::onReceiveBytes, view::onError)
        )
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


    override fun getTime() {
        getTimeDisposable =
            Observable.interval(0, TIME_QUERY_INTERVAL, TimeUnit.MILLISECONDS).doOnNext {
                CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.GET_TIME)
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { this.onErrorOccurred(it) }
                .subscribe()
        addDisposable(getTimeDisposable)
    }

    override fun stopTimeFetch() {
        getTimeDisposable.dispose()
    }

    override fun extractTimeData(context: Context, data: List<Char>, hardwareVersion: Int) {
        addDisposable(
            service.extractTimeData(context, data, hardwareVersion)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::displayTimeData, view::onError)
        )
    }

    override fun setTimeDate(time: DeviceTime, deviceDate: DeviceDate) {
        addDisposable(
            service.setTimeDate(time, deviceDate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::onTimeWriteResult, view::onError)
        )
    }

    override fun setReadData(data: List<Char>) {
        this.data.clear()
        this.data.addAll(data)
        service.setReadData(data)
    }

}