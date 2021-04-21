package com.mtkreader.data.reading

class Tonoff(value: Int) {

    var Toff = 0
    var Toffb = 0
    var Ton = 0
    var Tonb = 0
    var I=0
    init {
        Toff = value and 0x7FF
        Toffb = value shr 11 and 0x1
        Ton = value shr 12 and 0x7FF
        Tonb = value shr 23 and 0x1
        I=value and 0x00FFFFFF
    }


}