package com.mtkreader.commons.base

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.github.ivbaranov.rxbluetooth.BluetoothConnection
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.mtkreader.commons.Const
import com.mtkreader.contracts.BluetoothContract
import com.mtkreader.managers.DataManager
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


abstract class BaseBluetoothPresenter(private val view: BluetoothContract.View) : BasePresenter(view), BluetoothContract.Presenter, KoinComponent {

    protected val context: Context by inject()

    private val bluetoothManager: RxBluetooth by inject()
    private lateinit var connection: BluetoothConnection
    protected lateinit var socket: BluetoothSocket

    protected val dataManager = DataManager()

    private lateinit var timeoutDisposable: Disposable
    private lateinit var initCommunicationDisposable: Disposable


    override fun connectToDevice(device: BluetoothDevice) {
        addDisposable(
            bluetoothManager.connectAsClient(device, UUID.fromString(Const.BluetoothConstants.UUID))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSocketConnected, view::onError)
        )
        timeoutDisposable = Observable.timer(Const.BluetoothConstants.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
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
        this.socket = socket
        connection = BluetoothConnection(socket)
        view.onSocketConnected()
    }


    protected fun initDeviceCommunication() {
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


    protected fun readStream() {
        addDisposable(
            connection.observeByteStream()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::onByteReceive, view::onError)
        )
    }

    protected abstract fun onByteReceive(byte: Byte)

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