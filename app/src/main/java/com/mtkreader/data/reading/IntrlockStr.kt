package com.mtkreader.data.reading

class IntrlockStr {
    var wBitsOn: Int = 0
    var wBitsOff: Int = 0
    var PcCnfg: IntArray = IntArray(2)
    var PcCnfgW:Int=0
    constructor(wBitsOn: Int, wBitsOff: Int, PcCnfg: IntArray) {
        this.wBitsOn = wBitsOn
        this.wBitsOff = wBitsOff
        this.PcCnfg = PcCnfg
        this.PcCnfgW= (PcCnfg[0] shl 0) or (PcCnfg[1] shl 8)
    }

    constructor()
}
