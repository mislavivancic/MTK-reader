package com.mtkreader.presenters

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BasePresenter
import com.mtkreader.contracts.ConnectionContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject

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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribe(object : Consumer<BluetoothDevice> {
                    override fun accept(t: BluetoothDevice?) {
                        if (t != null)
                            view.onObservedDevice(t)
                    }

                })
            //.subscribe(this::onObservedDevicesConsumer)
        )

    }

    private fun onObservedDevicesConsumer(bluetoothDevice: BluetoothDevice?): Consumer<BluetoothDevice> {
        return object : Consumer<BluetoothDevice> {
            override fun accept(t: BluetoothDevice?) {
                TODO("Not yet implemented")
            }
        }
    }
}