package com.mtkreader.data.reading

class StrParFil {

    constructor(
        NYM1: Byte,
        NYM2: Byte,
        YM_B1: Int,
        YM_B2: Int,
        UTHMIN: Int,
        UTLMAX: Int,
        PERIOD: Int,
        FORMAT: Int,
        BROJ: Int,
        fre: Double
    ) {
        this.NYM1 = NYM1
        this.NYM2 = NYM2
        this.YM_B1 = YM_B1
        this.YM_B2 = YM_B2
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
    var YM_B1: Int = 0
    var YM_B2: Int = 0
    var UTHMIN: Int = 0
    var UTLMAX: Int = 0
    var PERIOD: Int = 0
    var FORMAT: Int = 0
    var BROJ: Int = 0
    var fre: Double = 0.0
}