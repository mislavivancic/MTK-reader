package com.mtkreader.presenters

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.github.ivbaranov.rxbluetooth.BluetoothConnection
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BasePresenter
import com.mtkreader.contracts.ReadingContract
import com.mtkreader.data.DeviceDate
import com.mtkreader.data.DeviceTime
import com.mtkreader.data.reading.TimeDate
import com.mtkreader.data.writing.DataRXMessage
import com.mtkreader.data.writing.DataTXTMessage
import com.mtkreader.utils.CommunicationUtil
import com.mtkreader.utils.DataUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

class ReadingPresenter(private val view: ReadingContract.View) : BasePresenter(view),
    ReadingContract.Presenter, KoinComponent {

    private val bluetoothManager: RxBluetooth by inject()
    private lateinit var connection: BluetoothConnection
    private lateinit var socket: BluetoothSocket

    override fun connectToDevice(device: BluetoothDevice) {
        addDisposable(
            bluetoothManager.connectAsClient(device, UUID.fromString(Const.BluetoothConstants.UUID))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSocketConnected, view::onError)
        )
    }

    private fun onSocketConnected(socket: BluetoothSocket) {
        this.socket = socket
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

    override fun closeConnection() {
        connection.closeConnection()
        clear()
    }

}