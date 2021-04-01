package com.mtkreader.utils

import android.bluetooth.BluetoothSocket

object CommunicationUtil {

    fun writeToSocket(socket: BluetoothSocket, data: String) {
        println("MESSAGES Sent-> $data")
        println("MESSAGES Sent-> ${hexStringToByteArray(data)}")
        val a = hexStringToByteArray(data)
        socket.outputStream.write(hexStringToByteArray(data))
    }

    fun writeToSocket(socket: BluetoothSocket, data: ByteArray) {
        println("MESSAGES Sent-> $data")
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