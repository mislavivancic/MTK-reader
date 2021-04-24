package com.mtkreader.utils

import android.bluetooth.BluetoothSocket
import com.mtkreader.commons.Const
import com.mtkreader.utils.DataUtils.byteArrayToHexString
import com.mtkreader.utils.DataUtils.hexStringToByteArray
import com.mtkreader.utils.DataUtils.hexToAscii
import net.alexandroid.utils.mylogkt.logI

object CommunicationUtil {

    fun writeToSocket(socket: BluetoothSocket, data: String) {
        logI(hexToAscii(data), customTag = Const.Logging.SENT)
        socket.outputStream.write(hexStringToByteArray(data))
    }

    fun writeToSocket(socket: BluetoothSocket, data: ByteArray) {
        logI(hexToAscii(byteArrayToHexString(data)), customTag = Const.Logging.SENT)
        socket.outputStream.write(data)
    }


}