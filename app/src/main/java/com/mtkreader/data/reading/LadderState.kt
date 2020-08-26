package com.mtkreader.data.reading

data class LadderState(
    val m_RelState: ByteArray,
    val m_RelNr: ByteArray,
    val m_isSeries: BooleanArray,
    val m_notsernotpar: BooleanArray,
    val m_conectshort: BooleanArray
)