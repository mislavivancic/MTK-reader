package com.mtkreader.data.reading

import kotlin.experimental.and


class UksPro {
    var akdan: Byte = 0
    var min: Byte = 0
    var off: Byte = 0
    var on: Byte = 0
    var sat: Byte = 0
    var rel: Byte = 0

    fun update() {
        min = min and 0x3F
        off = off and 0x01
        on = on and 0x01
        sat = sat and 0x1F
        rel = rel and 0x07
    }
}