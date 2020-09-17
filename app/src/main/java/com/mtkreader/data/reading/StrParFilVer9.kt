package com.mtkreader.data.reading

class StrParFilVer9 {

    constructor(
        NYM1: Byte,
        NYM2: Byte,
        K_V: Int,
        REZ: Int,
        UTHMIN: Int,
        UTLMAX: Int,
        PERIOD: Int,
        FORMAT: Int,
        BROJ: Int,
        fre: Double
    ) {
        this.NYM1 = NYM1
        this.NYM2 = NYM2
        this.K_V = K_V
        this.REZ = REZ
        this.UTHMIN = UTHMIN
        this.UTLMAX = UTLMAX
        this.PERIOD = PERIOD
        this.FORMAT = FORMAT
        this.BROJ = BROJ
        this.fre = fre
    }

    constructor()

    var NYM1: Byte = 0
    var NYM2: Byte = 0
    var K_V: Int = 0
    var REZ: Int = 0
    var UTHMIN: Int = 0
    var UTLMAX: Int = 0
    var PERIOD: Int = 0
    var FORMAT: Int = 0
    var BROJ: Int = 0
    var fre: Double = 0.0
}