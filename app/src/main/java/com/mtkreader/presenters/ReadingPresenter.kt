package com.mtkreader.presenters

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.github.ivbaranov.rxbluetooth.BluetoothConnection
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BasePresenter
import com.mtkreader.contracts.ReadingContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class ReadingPresenter(private val view: ReadingContract.View) : BasePresenter(view),
    ReadingContract.Presenter, KoinComponent {

    private val bluetoothManager: RxBluetooth by inject()
    private lateinit var connection: BluetoothConnection

    override fun connectToDevice(device: BluetoothDevice) {
        addDisposable(
            bluetoothManager.connectAsClient(device, UUID.fromString(Const.BluetoothConstants.UUID))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::onSocketConnected, view::onError)
        )
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