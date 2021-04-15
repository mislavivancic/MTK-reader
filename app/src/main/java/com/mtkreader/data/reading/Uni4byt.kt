package com.mtkreader.data.reading

import com.mtkreader.toPositiveInt

class Uni4byt {
    var i = 0
    var b: ByteArray? = null

    constructor(b: ByteArray) {
        this.b = b
        var mask1 = 0
        mask1 = b[3].toPositiveInt()
        mask1 = mask1 shl 24
        mask1 = mask1 and 0xFF000000.toInt()

        var mask2 = 0
        mask2 = b[2].toPositiveInt()
        mask2 = mask2 shl 16
        mask2 = mask2 and 0x00FF0000

        var mask3 = 0
        mask3 = b[1].toPositiveInt()
        mask3 = mask3 shl 8
        mask3 = mask3 and 0x0000FF00

        var mask4 = 0
        mask4 = b[0].toPositiveInt()
        mask4 = mask4 and 0x000000FF

        i = mask1 or mask2 or mask3 or mask4
    }

    constructor(i: Int) {
        this.i = i
        val b0 = (i and 0xFF).toByte()
        val b1 = (i shr 8 and 0xFF).toByte()
        val b2 = (i shr 16 and 0xFF).toByte()
        val b3 = (i shr 24 and 0xFF).toByte()
        this.b = byteArrayOf(b0, b1, b2, b3)

    }
}