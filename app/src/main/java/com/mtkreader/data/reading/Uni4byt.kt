package com.mtkreader.data.reading

class Uni4byt(val b: ByteArray) {
    var i = 0

    init {
        var mask1 = 0
        mask1 = b[3].toInt()
        mask1 = mask1 shl 32
        mask1 = mask1 and -0x1000000

        var mask2 = 0
        mask2 = b[2].toInt()
        mask2 = mask2 shl 16
        mask2 = mask2 and 0x00FF0000

        var mask3 = 0
        mask3 = b[1].toInt()
        mask3 = mask3 shl 8
        mask3 = mask3 and 0x0000FF00

        var mask4 = 0
        mask4 = b[0].toInt()
        mask4 = mask4 and 0x000000FF

        i = mask1 or mask2 or mask3 or mask4
    }
}