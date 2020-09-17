package com.mtkreader.services

import com.mtkreader.commons.Const
import com.mtkreader.contracts.DisplayDataContract
import com.mtkreader.data.reading.*
import kotlin.experimental.or
import kotlin.math.pow

class ProcessServiceImpl : DisplayDataContract.ProcessService {

    private var globalIndex = 0

    private val mline = ByteArray(256)
    private var m_Dateerr = 0
    private var m_cntxx = 1

    private var isCheck = false

    //set before
    private var mSoftwareVersionPri = 0
    private var m_HWVerPri = 0

    private var m_CFG: CfgParHwsw = CfgParHwsw()

    override fun processData(header: ByteArray, data: ByteArray): String {

        getVersions(header)

        // check how it's initialized
        mSoftwareVersionPri = 96

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

        val mPProgR1 = mutableListOf<Opprog>()
        val mPProgR2 = mutableListOf<Opprog>()
        val mPProgR3 = mutableListOf<Opprog>()
        val mPProgR4 = mutableListOf<Opprog>()
        for (i in 0..15) {
            mPProgR1.add(Opprog())
            mPProgR2.add(Opprog())
            mPProgR3.add(Opprog())
            mPProgR4.add(Opprog())
        }

        val mOp50Prij = Oprij50()
        val mOpPrij = Oprij()
        val mReallocs = mutableListOf<Rreallc>()

        var m_RelInterLock: List<IntrlockStr>

        globalIndex = 0


        when (mgaddr.group) {
            3 -> m_RelInterLock = getRelInterLock(dbuf)
            in 1..4 -> {
                globalIndex = 0
                val oPProg = getTparPar(dbuf)
                when (mgaddr.group) {
                    1 -> mPProgR1.add(mgaddr.objectt, oPProg)
                    2 -> mPProgR2.add(mgaddr.objectt, oPProg)
                    3 -> mPProgR3.add(mgaddr.objectt, oPProg)
                    4 -> mPProgR4.add(mgaddr.objectt, oPProg)
                }
            }
            5 -> {
                when (mgaddr.objectt) {
                    0 -> {
                        val wipers = getWipers(dbuf)
                    }
                    1 -> {
                        val pOnPOffRDat = getPonPoffRDat(dbuf)
                    }
                    2 -> {
                        val tlgAbsenceDat = getTlgAbsenceDat(dbuf)
                    }
                    3 -> {
                        val learningData = getLearningDat(dbuf)
                    }

                }
            }


            8 -> {
                val oprijParV9 = getOprijParV9(mgaddr, dbuf, mOp50Prij, mOpPrij, mReallocs)
            }
        }

    }

    private fun getOprijParV9(
        mgaddr: Mgaddr,
        dbuf: ByteArray,
        mOp50Prij: Oprij50,
        mOpPrij: Oprij,
        mReallocs: MutableList<Rreallc>
    ) {
        when (mgaddr.objectt) {
            0 -> {
                if (mSoftwareVersionPri >= 96)
                    getKlDatVer96(dbuf, mOp50Prij, mOpPrij, mReallocs)
            }
            1 -> if (mSoftwareVersionPri >= 96)
                getKl2VerDatVer96(dbuf, mOp50Prij, mOpPrij)
            2 -> {
                getDaljPar(dbuf, mOpPrij)
            }
        }
    }

    private fun getDaljPar(dbuf: ByteArray, mOpPrij: Oprij) {
        mOpPrij.apply {
            VOpRe.VakProR1 = setOprelI(dbuf)
            VOpRe.VakProR2 = setOprelI(dbuf)
            VOpRe.VakProR3 = setOprelI(dbuf)
            VOpRe.VakProR4 = setOprelI(dbuf)

            VOpRe.StaPrij = dbuf[globalIndex++]

            if (mSoftwareVersionPri < 95) {
                mOpPrij.ParFlags = dbuf[globalIndex++]

                StaR1PwON_OFF = dbuf[globalIndex++]
                StaR2PwON_OFF = dbuf[globalIndex++]
                StaR3PwON_OFF = dbuf[globalIndex++]
                StaR4PwON_OFF = dbuf[globalIndex++]
            }
        }

    }

    private fun getKl2VerDatVer96(dbuf: ByteArray, mOp50Prij: Oprij50, mOpPrij: Oprij) {
        var inCik = false

        if ((m_CFG.cID == 100) || (m_CFG.cID == 120))
            inCik = true

        if (inCik)
            mOpPrij.VDuzAdr = dbuf[globalIndex++]

        mOpPrij.PolUKRe = dbuf[globalIndex++]

        if (m_CFG.cID == 100)
            mOpPrij.VIdBr = dbuf[globalIndex++]

        mOp50Prij.RTCSinh = dbuf[globalIndex++]

        if (inCik)
            mOp50Prij.WDaySinh = dbuf[globalIndex++]

        if (m_CFG.cID == 100) {
            globalIndex++
            globalIndex++
            globalIndex++
            globalIndex++
        } else if (m_CFG.cID == 130 || m_CFG.cID == 0x8C) {
            globalIndex++
            globalIndex++
        }
        val sinhTimes = mutableListOf<Int>()
        for (i in 0..4) {
            sinhTimes.add(setOprel4I(dbuf))
        }
        mOp50Prij.SinhTime = sinhTimes.toIntArray()

        if (!inCik)
            return

        val b1 = dbuf[globalIndex++]
        mOpPrij.VCRel1Tu = dbuf[globalIndex++]
        mOpPrij.VC1R1 = setOprelI(dbuf)

        val b2 = dbuf[globalIndex++]
        mOpPrij.VCRel2Tu = dbuf[globalIndex++]
        mOpPrij.VC1R2 = setOprelI(dbuf)

        val b3 = dbuf[globalIndex++]
        mOpPrij.VCRel3Tu = dbuf[globalIndex++]
        mOpPrij.VC1R3 = setOprelI(dbuf)

        val b4 = dbuf[globalIndex++]
        mOpPrij.VCRel4Tu = dbuf[globalIndex++]
        mOpPrij.VC1R4 = setOprelI(dbuf)

        mOpPrij.CRelXSw = byteArrayOf(b1, b2, b3, b4)


        if (m_CFG.cID === 120 || m_HWVerPri == Const.Data.TIP_PS)
            return

        mOpPrij.VAdrR1 = setVerAdrVer9(dbuf)
        mOpPrij.VAdrR2 = setVerAdrVer9(dbuf)
        mOpPrij.VAdrR3 = setVerAdrVer9(dbuf)
        mOpPrij.VAdrR4 = setVerAdrVer9(dbuf)

    }

    private fun setVerAdrVer9(dbuf: ByteArray): Vadrr {
        val vadrr = Vadrr()
        val adrxx = dbuf[globalIndex++]
        return Vadrr().apply {
            VAdrRA = if (adrxx == 0.toByte()) 0 else getAdrNr(adrxx)
            VAdrRB = dbuf[globalIndex++]
            VAdrRC = dbuf[globalIndex++]
            VAdrRD = dbuf[globalIndex++]
        }
    }

    private fun getAdrNr(xxadr: Byte): Byte {
        var i: Byte = 0
        while (i < 8) {
            if (xxadr == Const.Data.bVtmask[i.toInt()]) {
                return ++i
            }
            i++
        }
        return 0.toByte()
    }

    private fun getKlDatVer96(
        dbuf: ByteArray,
        mOp50Prij: Oprij50,
        mOpPrij: Oprij,
        mReallocs: MutableList<Rreallc>
    ) {
        getKlDatVer9(dbuf, mOpPrij, mReallocs)
        mOp50Prij.apply {
            CPWBRTIME = setOprelI(dbuf)
            CLOGENFLGS = intArrayOf(setOprelI(dbuf), setOprelI(dbuf), setOprelI(dbuf))
        }
    }

    private fun getKlDatVer9(
        dbuf: ByteArray,
        mOpPrij: Oprij,
        mReallocs: MutableList<Rreallc>
    ) {
        mOpPrij.apply {
            KlOpR1 = setDlyRelDv9(dbuf)
            KlOpR2 = setDlyRelDv9(dbuf)
            KlOpR3 = setDlyRelDv9(dbuf)
            KlOpR4 = setDlyRelDv9(dbuf)
        }

        for (i in 0..3) {
            val reallc = Rreallc()
            reallc.rel_on = dbuf[globalIndex++]
            reallc.rel_off = dbuf[globalIndex++]
            mReallocs.add(reallc)
        }

    }

    private fun setDlyRelDv9(dbuf: ByteArray): Klopr {
        return Klopr().apply {
            KRelDela = setOprel4I(dbuf)
            KRelDelb = setOprel4I(dbuf)
        }
    }


    private fun getLearningDat(dbuf: ByteArray): List<StrLoadMng> {
        val strLoadMngs = mutableListOf<StrLoadMng>()
        for (i in 0..3) {
            val strLoadMng = getStrLoadMng(dbuf)
            strLoadMngs.add(strLoadMng)
        }

        return strLoadMngs
    }

    private fun getStrLoadMng(dbuf: ByteArray): StrLoadMng {
        return StrLoadMng().apply {
            status2 = dbuf[globalIndex++]
            relPos = dbuf[globalIndex++]
            TPosMin = setOprel3I(dbuf)
            TPosMax = setOprel3I(dbuf)
        }
    }


    private fun getTlgAbsenceDat(dbuf: ByteArray): List<TlgAbstr> {
        val tlgAbstrs = mutableListOf<TlgAbstr>()
        for (i in 0..3) {
            val tlgAbstr = getTlgAbstr(dbuf)
            tlgAbstrs.add(tlgAbstr)
        }

        return tlgAbstrs
    }

    private fun getTlgAbstr(dbuf: ByteArray): TlgAbstr {
        return TlgAbstr().apply {
            OnRes = dbuf[globalIndex++]
            TDetect = setOprel3I(dbuf)
            RestOn = dbuf[globalIndex++]
            OnTaExe = dbuf[globalIndex++]
        }
    }


    private fun getPonPoffRDat(dbuf: ByteArray): List<PonPoffStr> {
        val ponPoffStrs = mutableListOf<PonPoffStr>()
        for (i in 0..3) {
            val ponPoffStr = setPonPoffReData(dbuf)
            ponPoffStrs.add(ponPoffStr)
        }
        return ponPoffStrs
    }

    private fun setPonPoffReData(dbuf: ByteArray): PonPoffStr {
        return PonPoffStr().apply {
            OnPonExe = dbuf[globalIndex++]
            lperIgno = dbuf[globalIndex++]
            TminSwdly = setOprel3I(dbuf)
            TrndSwdly = setOprel3I(dbuf)
            Tlng = setOprel3I(dbuf)

            if ((Tlng and 0x800000) != 0)
                lperIgno = 0x80.toByte()
            else
                lperIgno = 0x00.toByte()

            Tlng = Tlng and 0x7fffff
            lOnPonExe = dbuf[globalIndex++]
            OnPoffExe = dbuf[globalIndex++]
            TBlockPrePro = setOprel3I(dbuf)
        }

    }

    private fun getWipers(dbuf: ByteArray): List<Wiper> {
        val wipers = mutableListOf<Wiper>()
        for (i in 0..3) {
            val wiper = Wiper()
            wiper.status = (0x80 + 0x20).toByte()
            setWiperRelData(wiper, dbuf)
            wipers.add(wiper)
        }
        return wipers
    }

    private fun setWiperRelData(wiper: Wiper, dbuf: ByteArray) {
        wiper.status = dbuf[globalIndex++]
        wiper.Tswdly = setOprel3I(dbuf)
        wiper.TWiper = setOprel3I(dbuf)
        wiper.TBlockPrePro = setOprel3I(dbuf)
    }

    private fun getTparPar(dbuf: ByteArray): Opprog {
        val oPProg = Opprog()


        val x = Unitimbyt()
        var nrTpar = 8

        val m_SWVerPri = 0x60
        val m_HWVerPri = 7
        val TIP_SPA = 2
        val NR_TPAR_SPA = 5
        val NR_TPAR_MAX = 14


        nrTpar = if (m_SWVerPri >= 90 && m_HWVerPri == TIP_SPA)
            NR_TPAR_SPA
        else if (m_SWVerPri >= 40 && m_SWVerPri < 96)
            NR_TPAR_MAX
        else {
            11
            //nrTpar = m_CFG.cNpar;
        }

        x.b[1] = dbuf[globalIndex++]

        if (m_SWVerPri >= 40)
            x.b[0] = dbuf[globalIndex++]

        x.updateI()

        oPProg.AkTim = x.i
        oPProg.DanPr = dbuf[globalIndex++]

        for (i in 0..nrTpar) {
            x.i = setOprel3I(dbuf)
            x.updateTB()
            oPProg.TPro[i] = x.t
        }
        return oPProg
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

    private fun setOprel3I(dbuf: ByteArray): Int {
        val b2 = dbuf[globalIndex++]
        val b1 = dbuf[globalIndex++]
        val b0 = dbuf[globalIndex++]
        val tempi = Uni4byt(byteArrayOf(b0, b1, b2, 0))
        return tempi.i
    }

    private fun setOprel4I(dbuf: ByteArray): Int {
        val b3 = dbuf[globalIndex++]
        val b2 = dbuf[globalIndex++]
        val b1 = dbuf[globalIndex++]
        val b0 = dbuf[globalIndex++]
        val tempi = Uni4byt(byteArrayOf(b0, b1, b2, b3))
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

    private fun getVersions(header: ByteArray) {
        val headString = header.toString(Charsets.UTF_8)

        var char: Char

        for ((cnt, version) in Const.Data.CTipPrij.withIndex()) {
            if (headString.contains(version, true)) {
                m_HWVerPri = cnt
                val indexOfVersionStart = headString.indexOf("V")
                char = headString.get(indexOfVersionStart + 1)
                if (char.isDigit())
                    mSoftwareVersionPri = (char - '0') * 10
                char = headString.get(indexOfVersionStart + 3)
                if (char.isDigit())
                    mSoftwareVersionPri += (char - '0')
                break
            }
        }

        val startIndexOfParams = headString.indexOf(";")

        val buff = strCopyHexToBuf(headString, startIndexOfParams + 1)
        m_CFG.cBrparam = buff[0]
        val a: Int = buff[1].toInt() and 0xFF
        val b: Int = buff[2].toInt() and 0xFF
        m_CFG.cID = 256 * a + b

        m_CFG.cPcbRev = buff[3]
        m_CFG.cNrel = buff[4]
        m_CFG.cRtc = buff[5]
        m_CFG.cNprog = buff[6]
        m_CFG.cNpar = buff[7]


    }

    private fun strCopyHexToBuf(headString: String, index: Int): List<Byte> {
        val buf = mutableListOf<Byte>()

        val len = (headString.length - index) / 2
        var i = 0

        var nIndex = index

        var lb: Byte
        var hb: Byte

        var glIndex = 0

        while (i++ < len) {
            hb = headString[nIndex++].toByte()
            lb = headString[nIndex++].toByte()

            if (hb == ')'.toByte() || lb == ')'.toByte()) {
                break
            }
            if (hb == '\r'.toByte() || lb == '\r'.toByte()) {
                break
            }

            hb = HextoD(hb, lb)
            buf.add(hb)
        }
        return buf
    }


    private fun HextoD(hb: Byte, lb: Byte): Byte {
        var mb: Byte
        mb = 0
        if (hb >= '0'.toByte() && hb <= '9'.toByte() || hb >= 'A'.toByte() && hb <= 'F'.toByte() ||
            lb >= '0'.toByte() && lb <= '9'.toByte() || lb >= 'A'.toByte() && lb <= 'F'.toByte()
        ) {
            mb =
                if (hb >= 'A'.toByte()) ((hb - '7'.toByte()) * 16).toByte() else ((hb - '0'.toByte()) * 16).toByte()
            mb =
                if (lb >= 'A'.toByte()) (mb + (lb - '7'.toByte())).toByte() else (mb + (lb - '0'.toByte())).toByte()
        }
        return mb
    }

}