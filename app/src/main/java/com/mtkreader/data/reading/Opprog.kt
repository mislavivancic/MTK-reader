package com.mtkreader.data.reading

class Opprog {


    init {
        var AkTim = 0
        var DanPr: Byte = 0
        val TPro = mutableListOf<Tonoff>()

        for (i in 0..14) {
            TPro.add(Tonoff(0))
        }
    }


}