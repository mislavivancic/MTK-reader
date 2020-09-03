package com.mtkreader.services

import com.mtkreader.commons.Const
import com.mtkreader.contracts.DisplayDataContract
import com.mtkreader.data.reading.IntrlockStr
import com.mtkreader.data.reading.Mgaddr
import com.mtkreader.data.reading.Opprog
import com.mtkreader.data.reading.Uni4byt
import kotlin.experimental.or
import kotlin.math.pow

class ProcessServiceImpl : DisplayDataContract.ProcessService {

    private var globalIndex = 0

    private val mline = ByteArray(256)
    private var m_Dateerr = 0
    private var m_cntxx = 1

    private var isCheck = false

    override fun processData(data: ByteArray): String {


        mline[0] = 0

        while (hasNextLine(data)) {
            if (mline[0] != Const.Tokens.END_TOKEN.toByte())
                getLineData()
            else
                break
        }


        return ""
    }

    private fun getLineData() {
        var i = 0
        val m_gaddr: Mgaddr = Mgaddr(0)
        var bb: Char

        while (i < 5) {
            if (mline[i] == '('.toByte())
                break
            bb = HtoB(mline[i++].toChar())
            if (isCheck) {
                m_gaddr.i = m_gaddr.i shl 4
                m_gaddr.i = m_gaddr.i or bb.toInt()
            } else
                m_Dateerr++
        }
        m_gaddr.update()


        if (mline[i] != ')'.toByte())
            m_Dateerr++

        i = 5
        val dbuf = ByteArray(128)


        for (j in 0..128) {
            var k = 2
            while (k != 0) {
                k--
                if (mline[i] == ')'.toByte())
                    break
                bb = HtoB(mline[i++].toChar())
                if (isCheck) {
                    dbuf[j] = (dbuf[j] * (2f.pow(4)).toInt()).toByte()
                    dbuf[j] = dbuf[j] or bb.toByte()
                } else
                    m_Dateerr++
            }
            if (mline[i] == ')'.toByte())
                break
        }

        unpackDatV9(dbuf, m_gaddr)
    }

    private fun unpackDatV9(dbuf: ByteArray, mgaddr: Mgaddr) {

        var m_RelInterLock: List<IntrlockStr>
        globalIndex = 0

        when (mgaddr.group) {
            0 -> {
            }
            3 -> m_RelInterLock = getRelInterLock(dbuf)
            in 0..5 -> {
                globalIndex = 0
            }
        }

    }

    private fun getTparPar(rel: Int, nProNum: Int, dbuf: ByteArray): List<List<Opprog>> {
        val listOfOpprog = mutableListOf(Opprog())

    }

    private fun getRelInterLock(dbuf: ByteArray): List<IntrlockStr> {
        val intrLockStrList = mutableListOf<IntrlockStr>()
        for (rel in 0..3) {
            val intrLockStr =
                IntrlockStr(
                    setOprelI(dbuf),
                    setOprelI(dbuf),
                    intArrayOf(setOprelI(dbuf), setOprelI(dbuf))
                )
            intrLockStrList.add(intrLockStr)
        }
        return intrLockStrList

    }

    private fun setOprelI(dbuf: ByteArray): Int {
        val b1 = dbuf[globalIndex++]
        val b0 = dbuf[globalIndex++]
        val tempi = Uni4byt(byteArrayOf(b0, b1, 0, 0))
        return tempi.i
    }


    private fun hasNextLine(data: ByteArray): Boolean {
        var i = 0
        while (m_cntxx < data.size) {
            if (data[m_cntxx] == 0x0D.toByte()) {
                mline[i++] = data[m_cntxx++]

                if (data[m_cntxx] == 0x0A.toByte()) {
                    mline[i++] = data[m_cntxx++]
                    mline[i] = 0
                    return true
                } else {
                    mline[i] = 0
                    m_Dateerr++
                    return false
                }
            }
            mline[i++] = data[m_cntxx++]
        }
        m_Dateerr++
        return false
    }

    private fun HtoB(ch: Char): Char {
        if (ch in '0'..'9') {
            isCheck = true
            return (ch - '0').toChar()
        }
        if (ch in 'A'..'F') {
            isCheck = true
            return (ch - 'A' + 0xA).toChar()
        }
        isCheck = false
        return ch
    }
}