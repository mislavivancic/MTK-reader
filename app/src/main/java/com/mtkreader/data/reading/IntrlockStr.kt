package com.mtkreader.data.reading

class IntrlockStr {
    var wBitsOn: Int = 0
    var wBitsOff: Int = 0
    var PcCnfg: IntArray = IntArray(2)

    constructor(wBitsOn: Int, wBitsOff: Int, PcCnfg: IntArray) {
        this.wBitsOn = wBitsOn
        this.wBitsOff = wBitsOff
        this.PcCnfg = PcCnfg
    }

    constructor()
}
