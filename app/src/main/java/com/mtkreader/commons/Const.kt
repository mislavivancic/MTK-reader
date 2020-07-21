package com.mtkreader.commons

object Const {
    object RequestCode {
        const val REQUEST_ENABLE_BT = 1
    }

    object PermissionCode {
        const val GRANTED = 0
        const val DENIED = -1
    }

    object Error {
        const val BT_NOT_SUPPORTED = "Bluetooth is not supported on this device!"
        const val BT_REQUIRED = "Bluetooth is required!"
    }

    object BluetoothConstants {
        const val UUID = "00001101-0000-1000-8000-00805f9b34fb"
    }
}