package com.mtkreader.data.reading

data class Oprij50(
    val TlgRel1: Telegrel,
    val TlgRel2: Telegrel,
    val TlgRel3: Telegrel,
    val TlgRel14: Telegrel,
    val tlg: List<Tlg>,
    val TlgVerAdr2: VadrTreiler,
    val SinhTime: IntArray,
    val RTCSinh: Byte,
    val WDaySinh: Byte,
    val CPWBRTIME: Int,
    val CLOGENFLGS: IntArray
)