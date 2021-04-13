package com.mtkreader.data.reading

class Oprij50 {
    var TlgRel1: Telegrel = Telegrel()
    var TlgRel2: Telegrel = Telegrel()
    var TlgRel3: Telegrel = Telegrel()
    var TlgRel4: Telegrel = Telegrel()
    var tlg: List<Tlg> = mutableListOf(Tlg(), Tlg(), Tlg(), Tlg(), Tlg(), Tlg(), Tlg(), Tlg())
    var TlgVerAdr2: VadrTreiler = VadrTreiler()
    var SinhTime: IntArray = IntArray(13)
    var RTCSinh: Byte = 0
    var WDaySinh: Byte = 0
    var CPWBRTIME: Int = 0
    var CLOGENFLGS: ShortArray = ShortArray(3)
}