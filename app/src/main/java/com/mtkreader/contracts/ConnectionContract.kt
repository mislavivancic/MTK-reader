package com.mtkreader.contracts

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import androidx.fragment.app.Fragment
import com.mtkreader.commons.base.AutoDisposePresenter
import com.mtkreader.commons.base.ErrorHandlingFragment

interface ConnectionContract {

    interface View : ErrorHandlingFragment {
        fun provideFragment(): Fragment

        fun onBluetoothInit()
        fun onObservedDevice(device: BluetoothDevice)
        fun onConnectedDevices(devices: Set<BluetoothDevice>?)
        fun onSocketConnected(socket: BluetoothSocket)

        fun onError(throwable: Throwable)
    }

    interface Presenter : AutoDisposePresenter {
        fun initBluetooth()
        fun observeDevices()
        fun getConnectedDevices()
        fun connectToDevice(device: BluetoothDevice)
    }
}