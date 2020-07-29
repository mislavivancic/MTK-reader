package com.mtkreader.utils

import android.bluetooth.BluetoothSocket
import com.mtkreader.commons.Const

object CommunicationUtil {

    fun writeToSocket(socket: BluetoothSocket, data: String) {
        socket.outputStream.write(hexStringToByteArray(Const.DeviceConstants.FIRST_INIT))
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                    + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}