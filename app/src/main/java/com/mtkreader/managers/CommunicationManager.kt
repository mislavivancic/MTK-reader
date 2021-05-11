package com.mtkreader.managers

import com.mtkreader.commons.Const
import com.mtkreader.data.writing.DataRXMessage
import com.mtkreader.exceptions.BccException
import com.mtkreader.exceptions.CommunicationException
import com.mtkreader.utils.DataUtils
import com.mtkreader.utils.DataUtils.hexToAscii
import net.alexandroid.utils.mylogkt.logI
import kotlin.experimental.xor

class CommunicationManager {


    private val data = mutableListOf<Byte>()
    private var readMessageData = DataRXMessage()
    private val readData = mutableListOf<Byte>()
    private var retryCount = 0
    private var readoutType: Byte = 0
    var timeOut = 0L

    fun addData(byte: Byte) {
        data.add(byte)
    }

    fun waitForMessage(type: Byte = 0): DataRXMessage {
        data.clear()
        readoutType = type
        retryCount = 0
        readMessageData = DataRXMessage()
        timeOut = System.currentTimeMillis() + 1500
        do {
            if (System.currentTimeMillis() > timeOut) {
                throw CommunicationException()
            }

        } while (!endOfMessage())
        logI(hexToAscii(DataUtils.byteArrayToHexString(readMessageData.getBufferData().toByteArray())), customTag = Const.Logging.RECEIVED)
        return readMessageData
    }


    private fun endOfMessage(): Boolean {
        try {

            while (true) {
                timeOut = System.currentTimeMillis() + 1500

                if (data.isNotEmpty()) {
                    readData.clear()
                    readData.addAll(data)
                    readMessageData = DataRXMessage().apply {
                        type = readoutType
                    }
                    for (dataByte in readData) {
                        if (readMessageData.status == Const.Data.ETX || readMessageData.status == Const.Data.EOT) {
                            readMessageData.bcc = readMessageData.bcc xor dataByte
                            if (readMessageData.bcc == 0.toByte()) {
                                readMessageData.status = Const.Data.COMPLETE
                            } else {
                                readMessageData.proterr = 0xCC.toByte()
                                throw BccException()
                            }
                            return true
                        } else {
                            if (readMessageData.status == Const.Data.SOH || readMessageData.status == Const.Data.STX)
                                readMessageData.bcc = readMessageData.bcc xor dataByte

                            when (dataByte) {
                                Const.Data.SOH -> {
                                    if (readMessageData.count == 0)
                                        readMessageData.status = Const.Data.SOH
                                    else
                                        readMessageData.proterr = Const.Data.SOH
                                    readMessageData.buffer[readMessageData.count++] = dataByte
                                }
                                Const.Data.STX -> {
                                    if (readMessageData.status == Const.Data.SOH || readMessageData.count == 0)
                                        readMessageData.status = Const.Data.STX
                                    else
                                        readMessageData.proterr = Const.Data.STX
                                    readMessageData.buffer[readMessageData.count++] = dataByte
                                }
                                0x0D.toByte() -> {
                                    readMessageData.buffer[readMessageData.count++] = dataByte
                                    if (readMessageData.type == 0.toByte()) readMessageData.crlf = 0x0D
                                }
                                0x0A.toByte() -> {
                                    readMessageData.buffer[readMessageData.count++] = dataByte
                                    if (readMessageData.type == 0.toByte()) {
                                        if (readMessageData.crlf == 0x0D.toByte())
                                            readMessageData.crlf = 0x0A
                                        readMessageData.status = Const.Data.COMPLETE
                                        return true
                                    }
                                }
                                Const.Data.ETX -> {
                                    readMessageData.status = Const.Data.ETX
                                }
                                Const.Data.EOT -> {
                                    readMessageData.status = Const.Data.EOT
                                }
                                Const.Data.ACK -> {
                                    readMessageData.status = Const.Data.ACK
                                    return true
                                }
                                Const.Data.NAK -> {
                                    readMessageData.status = Const.Data.NAK
                                    return true
                                }
                                else -> {
                                    readMessageData.buffer[readMessageData.count++] = dataByte
                                    if (readMessageData.count >= 2048 * 4) {
                                        readMessageData.proterr = 0x55
                                        return false
                                    }
                                }

                            }
                        }
                    }
                } else {
                    retryCount++
                    Thread.sleep(700)
                }
                if (retryCount > 5)
                    return false
            }
        } catch (e: NullPointerException) {
            return false
        }


    }
}
