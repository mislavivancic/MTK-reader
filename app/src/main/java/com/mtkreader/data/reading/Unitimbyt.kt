package com.mtkreader.data.reading

class Unitimbyt {

    var t: Tonoff = Tonoff(0)
    var i = 0
    var b: ByteArray = ByteArray(4)

    fun updateI() {
        i = 0x0
        var top1 = 0
        top1 = b[3].toInt()
        top1 = top1 shl 32 // TODO 24 shift
        top1 = top1 and -0x1000000

        var top2 = 0
        top2 = b[2].toInt()
        top2 = top2 shl 16
        top2 = top2 and 0x00FF0000

        var top3 = 0
        top3 = b[1].toInt()
        top3 = top3 shl 8
        top3 = top3 and 0x0000FF00

        var top4 = 0
        top4 = b[0].toInt()
        top4 = top4 and 0x000000FF

        i = i or top1 or top2 or top3 or top4
    }

    fun updateTB() {
        t = Tonoff(i)
        b[3] = (i shr 32 and 0xFF).toByte()
        b[2] = (i shr 16 and 0xFF).toByte()
        b[1] = (i shr 8 and 0xFF).toByte()
        b[0] = (i and 0xFF).toByte()
    }
}