package com.mtkreader.data.reading

// union
class UniUksByt {
    var p: UksPro = UksPro()
    var i = 0
    var b = ByteArray(4)

    fun updateP() {
        p.akdan = ((i shr 16) and 0xFF).toByte()

        p.min = ((i shr 10) and 0x3F).toByte()
        p.off = ((i shr 9) and 0x01).toByte()
        p.on = ((i shr 8) and 0x01).toByte()

        p.on = ((i shr 3) and 0x1F).toByte()
        p.on = (i and 0x07).toByte()
    }
}