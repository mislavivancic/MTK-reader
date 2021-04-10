package com.mtkreader.utils

import android.bluetooth.BluetoothSocket
import com.mtkreader.commons.Const
import net.alexandroid.utils.mylogkt.logI

object CommunicationUtil {

    fun writeToSocket(socket: BluetoothSocket, data: String) {
        logI("${hexStringToByteArray(data)}", customTag = Const.Logging.SENT)
        socket.outputStream.write(hexStringToByteArray(data))
    }

    fun writeToSocket(socket: BluetoothSocket, data: ByteArray) {
        logI("$data", customTag = Const.Logging.SENT)
        socket.outputStream.write(data)
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