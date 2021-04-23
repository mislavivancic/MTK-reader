package com.mtkreader.data.reading

class Tonoff(value: Int) {

    var Toff = 0
    var Toffb = 0
    var Ton = 0
    var Tonb = 0
    var I=0
    var bToffb:Boolean=false //bool od Toffb
    var bTonb:Boolean=false //bool od Tonb
    init {
        Toff = value and 0x7FF
        Toffb = value shr 11 and 0x1
        Ton = value shr 12 and 0x7FF
        Tonb = value shr 23 and 0x1
        I=value and 0x00FFFFFF
        bToffb = if (Toffb != 0) true else false
        bTonb = if (Tonb != 0) true else false
    }


}