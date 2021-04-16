package com.mtkreader.data.reading

class Opprog {
    var AkTim = 0
    var DanPr: Byte = 0
    val TPro = Array(14){Tonoff(0x00800800)} //   mutableListOf<Tonoff>() //TODO array 14

    init {
        //for (i in 0..13)
        //    TPro.add(Tonoff(0))

    }
}