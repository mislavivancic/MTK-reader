package com.mtkreader.data.reading

class Mgaddr(i: Int) {
    var i = 0
    var reg = 0
    var typ = 0
    var objectt = 0
    var group = 0

    init {
        reg = i and 0x1F
        typ = i shr 5 and 0x7
        objectt = i shr 8 and 0x17
        group = i shr 12 and 0x17
    }

    fun update() {
        reg = i and 0x1F
        typ = i and 0xE0 shr 5
        objectt = i and 0xF00 shr 8
        group = i and 0xF000 shr 12
    }
}