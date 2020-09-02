package com.mtkreader.commons

object Const {
    object RequestCode {
        const val REQUEST_ENABLE_BT = 1
    }


    object Tokens {
        const val END_TOKEN = 33.toByte().toChar()
    }

    object Extras {
        const val SOCKET_EXTRA = "SOCKET_EXTRA"
        const val DEVICE_EXTRA = "DEVICE_EXTRA"
        const val DATA_EXTRA = "DATA_EXTRA"
    }

    object SharedPrefKeys {
        const val READ_DATA_KEY = "READ_DATA_KEY"
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

    object DeviceConstants {
        const val NAME = "HELM"

        const val FIRST_INIT = "2F3F210D0A"
        const val SECOND_INIT = "063034360D0A"
        const val ACK = "015503560D0A"
    }
}