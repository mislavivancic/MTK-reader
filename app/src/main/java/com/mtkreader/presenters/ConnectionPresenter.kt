package com.mtkreader.presenters

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import com.github.ivbaranov.rxbluetooth.BluetoothConnection
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BasePresenter
import com.mtkreader.contracts.ConnectionContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class ConnectionPresenter(private val view: ConnectionContract.View) : BasePresenter(view),
    ConnectionContract.Presenter, KoinComponent {

    private val bluetoothManager: RxBluetooth by inject()


    override fun initBluetooth() {
        if (!bluetoothManager.isBluetoothAvailable) {
            onErrorOccurred(Error(Const.Error.BT_NOT_SUPPORTED))
        } else {
            if (!bluetoothManager.isBluetoothEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                view.provideFragment().startActivityForResult(
                    enableBtIntent, Const.RequestCode.REQUEST_ENABLE_BT
                )
            } else {
                view.onBluetoothInit()
            }
        }
    }

    override fun observeDevices() {
        addDisposable(
            bluetoothManager.observeDevices()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { device ->
                    if (device != null)
                        view.onObservedDevice(device)
                }
        )

    }


    override fun getConnectedDevices() {
        view.onConnectedDevices(bluetoothManager.bondedDevices)
    }

    override fun connectToDevice(device: BluetoothDevice) {
        addDisposable(
            bluetoothManager.connectAsClient(device, UUID.fromString(Const.BluetoothConstants.UUID))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::onSocketConnected, view::onError)
        )
    }

    override fun readStream(socket: BluetoothSocket) {
        val connection = BluetoothConnection(socket)

        addDisposable(
            connection.observeByteStream()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(view::onReceiveBytes, view::onError)
        )
    }

}