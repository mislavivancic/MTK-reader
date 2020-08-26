package com.mtkreader.data.reading

data class LadderNets(
    val Idx: Int,
    val RelState: ByteArray,
    val RelNr: ByteArray,
    val isSeries: BooleanArray,
    val notsernotpar: BooleanArray,
    val conectshort: BooleanArray
)