package com.mtkreader.data.reading

class LadderState {
    var m_RelState: MutableList<Byte> = mutableListOf(0, 0, 0, 0)
    var m_RelNr: MutableList<Byte> = mutableListOf(0, 0, 0, 0)
    var m_isSeries: MutableList<Boolean> = mutableListOf(false, false, false, false)
    var m_notsernotpar: MutableList<Boolean> = mutableListOf(false, false, false, false)
    var m_conectshort: MutableList<Boolean> = mutableListOf(false, false, false, false)
}