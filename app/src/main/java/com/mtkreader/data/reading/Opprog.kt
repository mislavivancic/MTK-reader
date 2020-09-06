package com.mtkreader.data.reading

class Opprog {
    var AkTim = 0
    var DanPr: Byte = 0
    val TPro = mutableListOf<Tonoff>()

    init {
        for (i in 0..13)
            TPro.add(Tonoff(0))

    }
}