package com.mtkreader.services

import android.content.Context
import android.widget.Toast
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.Const.Data.TIP_PASN
import com.mtkreader.compare
import com.mtkreader.contracts.ParamsWriteContract
import com.mtkreader.data.reading.*
import com.mtkreader.toPositiveInt
import com.mtkreader.utils.DataUtils
import com.mtkreader.utils.DataUtils.isHexadecimal
import com.mtkreader.utils.DataUtils.removeNonAlphanumeric
import io.reactivex.Completable
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.Long
import kotlin.math.pow

class ParamsWriteService : ParamsWriteContract.Service, KoinComponent {

    private val context: Context by inject()

    private var globalIndex = 0

    private var mTip = 0
    private var mHardwareVersion = 0
    private var mSoftwareVersion = 0
    private var mParFiltera = StrParFil()
    private val mParFilteraCF = StrParFilVer9()
    private var mBrojRast = 0
    private var mUtfPosto = 0.0
    private val UTFREFP = 0.9
    private var mBrUpKalendara: Byte = 0
    private val mCfg = CfgParHwsw()
    private var mFileComment = ""
    private val mOprij = Oprij()
    private val mOp50rij = Oprij50()
    private val mRealloc = Array(4) { Rreallc() }
    private val mTelegSync = Array(13) { Telegram() }
    private val mPProgR1 = Array(16) { Opprog() }
    private val mPProgR2 = Array(16) { Opprog() }
    private val mPProgR3 = Array(16) { Opprog() }
    private val mPProgR4 = Array(16) { Opprog() }
    private var mPBuff = ByteArray(256)
    private var mPraznici = PrazniciStr()
    private var mWipersRx = Array(4) { Wiper() }
    private var mPonPoffRx = Array(4) { PonPoffStr() }
    private var mTelegAbsenceRx = Array(4) { TlgAbstr() }
    private var mLearningRx = Array(4) { StrLoadMng() }
    private var mRelInterlock = Array(4) { IntrlockStr() }
    private var mKalendar = Array(72) { StKalend() }
    private var mInitRelSetProg = InitRelSetting()
    private var mUkls = Ukls()
    private var mCFileParData = RecFilParStr()


    private val addressMap = mutableMapOf<String, ByteArray>()

    companion object {
        private const val FILE_TOKEN = "//Programiranje"
        private const val KALBLOCK_SIZE = 0x30
        private const val NR_KAL_IN_BLOCK = KALBLOCK_SIZE / 8

    }

    override fun extractFileData(fileLines: List<String>): Completable {
        return Completable.fromAction { extract(fileLines) }
    }

    private fun extract(fileLines: List<String>) {
        if (!fileLines.first().startsWith(FILE_TOKEN, ignoreCase = true)) {
            throw Error(context.getString(R.string.not_mtk_file))
        }
        var line = fileLines.getOrNull(1)
        extractDeviceInfo(line)
        line = fileLines.getOrNull(2)
        extractConstants(line)
        line = fileLines.getOrNull(3)
        extractComment(line)
        fillAddressMap(fileLines.subList(4, fileLines.size))

        fillBuffer("8080")
        if (mSoftwareVersion >= 80)
            getKlDatVer9file()



        fillBuffer("8180")
        getVerAdrParVer6()

        fillBuffer("8280")
        getPg2Par()

        fillBuffer("9080")
        fillTelegRel(mOp50rij.TlgRel1)

        fillBuffer("9180")
        fillTelegRel(mOp50rij.TlgRel2)

        fillBuffer("9280")
        fillTelegRel(mOp50rij.TlgRel3)

        fillBuffer("9380")
        fillTelegRel(mOp50rij.TlgRel4)

        fillBuffer("9480")
        fillTelegramTlg(mOp50rij.tlg[0])

        fillBuffer("9580")
        fillTelegramTlg(mOp50rij.tlg[3])

        fillBuffer("9680")
        fillTelegramTlg(mOp50rij.tlg[5])

        fillBuffer("9880")
        fillTelegram(mTelegSync[0])

        fillBuffer("9980")
        fillTelegram(mTelegSync[2])

        fillBuffer("9A80")
        fillTelegram(mTelegSync[5])

        fillBuffer("9B80")
        fillTelegram(mTelegSync[8])

        fillBuffer("9C80")
        fillTelegram(mTelegSync[10])

        fillBuffer("0180")
        mOprij.VAdrPrij = setOprel3I()


        var rel = 1
        var pPProg = Opprog()
        val x = Unitimbyt()
        do {
            var mNProNum = 0
            do {
                val address = String.format("%01X%01X80", rel, mNProNum)
                fillBuffer(address)
                // TODO check if mPBuff len < 52 then throw error
                pPProg = getPProg(rel, mNProNum)
                x.b[1] = mPBuff[globalIndex++]
                x.b[0] = mPBuff[globalIndex++]
                x.updateI()
                pPProg.AkTim = x.i and 0xFFFC
                pPProg.DanPr = mPBuff[globalIndex++]
                for (i in 0 until 14) {
                    x.i = setOprel3I()
                    x.updateTB()
                    pPProg.TPro[i] = x.t

                }
                mNProNum++
            } while (mNProNum < 16)
            rel++
        } while (rel < 5)


        fillBuffer("7080")
        getUklsPar()

        fillBuffer("7280")
        getAsatPar()

        fillBuffer("7380")
        getPrazDaniPar()

        if (mSoftwareVersion >= 95) {
            fillBuffer("5080")
            getWiperData() //TODO duplicate

            fillBuffer("5180")
            getPonPoffRDat() // TODO duplicate

            fillBuffer("5280")
            getTlgAbsenceDat() // TODO duplicate

            fillBuffer("5380")
            getStrLoadMng()// TODO duplicate

            fillBuffer("0380")
            getRelInterLock()// TODO duplicate
        }

        fillBuffer("C080")
        getFriRParVer9()// TODO duplicate

        if (mSoftwareVersion >= 80) {
            mBrUpKalendara = 0
            fillBuffer("A080")
            getBrUpKalendara()
            try {
                for (i in 0 until NR_KAL_IN_BLOCK)
                    fillKalendar(mKalendar[i])
            } catch (ex: ArrayIndexOutOfBoundsException) {
                // failed to mem cpy
            }
            if (mBrUpKalendara > 5) {
                val sizeOfStKalend = 8
                var nrBlok: Int = (mBrUpKalendara - 5) / (sizeOfStKalend / (NR_KAL_IN_BLOCK - 1))
                if ((mBrUpKalendara - 5) / (sizeOfStKalend / (NR_KAL_IN_BLOCK - 1)) != 0) nrBlok++

                var cnt = 1
                do {
                    val address = String.format("A%01X80", cnt)
                    fillBuffer(address)
                    rel = (cnt * NR_KAL_IN_BLOCK) - 1
                    fillKalendar(mKalendar[rel])
                    cnt++
                } while (cnt < nrBlok)
            }
        }

        if (mSoftwareVersion >= 96) {
            fillBuffer("G6")
            getAfterProgSetting(true)

            fillBuffer("G7")
            readCreatedId(true)
        }
        println()
    }

    private fun readCreatedId(buff: Boolean) {
        if (!buff) return

        for (i in 0 until RecFilParStr.PARID_SIZE)
            mCFileParData.CreateSite[i] = mPBuff[globalIndex++]

        for (i in 0 until RecFilParStr.PARID_SIZE)
            mCFileParData.IDCreate[i] = mPBuff[globalIndex++]

        for (i in 0 until RecFilParStr.PARIDFILE_SIZE)
            mCFileParData.IDFile[i] = mPBuff[globalIndex++]

    }

    private fun getAfterProgSetting(buff: Boolean) {
        if (buff) {
            mInitRelSetProg.setPar = mPBuff[globalIndex++]
            for (i in 0 until 4)
                mInitRelSetProg.status[i] = mPBuff[globalIndex++]
        }
    }


    private fun fillKalendar(kalendar: StKalend) {
        with(kalendar) {
            broj = mPBuff[globalIndex++]
            danuTj = mPBuff[globalIndex++]
            akPr1do8 = mPBuff[globalIndex++]
            akPr9do15 = mPBuff[globalIndex++]
            fillDatum(PocDan)
        }
    }

    private fun fillDatum(datum: Datum) {
        datum.mje = mPBuff[globalIndex++]
        datum.dan = mPBuff[globalIndex++]
    }

    private fun getBrUpKalendara() {
        globalIndex++
        globalIndex++
        globalIndex++
        globalIndex++
        globalIndex++
        globalIndex++
        globalIndex++
        mBrUpKalendara = mPBuff[globalIndex++]
    }

    private fun getFriRParVer9() {
        with(mParFilteraCF) {
            NYM1 = mPBuff[globalIndex++]
            NYM2 = mPBuff[globalIndex++]
            K_V = setOprelI()
            REZ = setOprelI()
            UTHMIN = setOprelI()
            UTLMAX = setOprelI()
            PERIOD = setOprelI()
            FORMAT = setOprelI()
            BROJ = mPBuff[globalIndex++].toInt()
            globalIndex++
            mBrojRast = setOprelI()
            setUfPosto()
        }
    }

    private fun setUfPosto() {
        val broj = mParFilteraCF.BROJ
        if (broj >= 0) {
            var KvUt = mParFilteraCF.K_V
            if (KvUt == 0) {
                KvUt = DataUtils.getTbParFilteraVer9()[broj].K_V
                Toast.makeText(context, "Error 'Kv = 0' ", Toast.LENGTH_SHORT).show()
            }

            val utth = mParFilteraCF.UTHMIN.toDouble()
            var uthMin = utth / KvUt.toDouble()

            uthMin *= 1.002
            var utlMax = mParFilteraCF.UTLMAX.toDouble() / KvUt.toDouble()
            utlMax *= 1.002

            var uthMinRef = 0.0
            val ith = getFrUTHDefVer9(broj)
            if (ith != 0) {
                uthMinRef = ith.toDouble()
                mUtfPosto = (utth * UTFREFP) / uthMinRef
            } else
                mUtfPosto = 0.0
        }
    }

    private fun getFrUTHDefVer9(broj: Int): Int {
        var uth = 0
        if (broj >= 0)
            uth = DataUtils.getTbParFilteraVer9()[broj].UTHMIN

        return uth
    }

    private fun getRelInterLock() {
        for (i in 0 until 4) {
            with(mRelInterlock[i]) {
                wBitsOn = setOprelI()
                wBitsOff = setOprelI()
                PcCnfg[0] = setOprelI()
                PcCnfg[1] = setOprelI()
            }
        }
    }

    private fun getStrLoadMng() {
        for (i in 0 until 4) {
            with(mLearningRx[i]) {
                Status = mPBuff[globalIndex++]
                relPos = mPBuff[globalIndex++]
                TPosMin = setOprel3I()
                TPosMax = setOprel3I()
            }
        }
    }

    private fun getTlgAbsenceDat() {
        for (i in 0 until 4) {
            with(mTelegAbsenceRx[i]) {
                OnRes = mPBuff[globalIndex++]
                TDetect = setOprel3I()
                RestOn = mPBuff[globalIndex++]
                OnTaExe = mPBuff[globalIndex++]
            }
        }
    }

    private fun getPonPoffRDat() {
        setPonPoffReData(mPonPoffRx[0])
        setPonPoffReData(mPonPoffRx[1])
        setPonPoffReData(mPonPoffRx[2])
        setPonPoffReData(mPonPoffRx[3])
    }

    private fun setPonPoffReData(ponPoffStr: PonPoffStr) {
        with(ponPoffStr) {
            OnPonExe = mPBuff[globalIndex++]
            lperIgno = mPBuff[globalIndex++]
            TminSwdly = setOprel3I()
            TrndSwdly = setOprel3I()
            Tlng = setOprel3I()

            if ((Tlng and 0x800000) != 0)
                lperIgno = 0x80.toByte()
            else
                lperIgno = 0x00.toByte()

            Tlng = Tlng and 0x7fffff
            lOnPonExe = mPBuff[globalIndex++]
            OnPoffExe = mPBuff[globalIndex++]
            TBlockPrePro = setOprel3I()
        }

    }


    private fun getWiperData() {
        for (i in 0 until 4) {
            mWipersRx[i].status = (0x80 + 0x20).toByte()
            setWiperRelData(mWipersRx[i])
        }
    }

    private fun setWiperRelData(wiper: Wiper) {
        wiper.status = mPBuff[globalIndex++]
        wiper.Tswdly = setOprel3I()
        wiper.TWiper = setOprel3I()
        wiper.TBlockPrePro = setOprel3I()
    }

    private fun getPrazDaniPar() {
        var brpraz: Int = mPBuff[globalIndex++].toPositiveInt()
        brpraz = brpraz shl 8
        brpraz = brpraz or mPBuff[globalIndex++].toPositiveInt()
        mPraznici.brpraz = brpraz.toShort()
        if (brpraz <= 16) {
            for (i in 0 until brpraz) {
                mPraznici.datum[i].mje = mPBuff[globalIndex++]
                mPraznici.datum[i].dan = mPBuff[globalIndex++]
            }
        }

    }

    private fun getAsatPar() {
        with(mOprij) {
            StaAsat = mPBuff[globalIndex++]
            AsatKorOn = mPBuff[globalIndex++]
            AsatKorOff = mPBuff[globalIndex++]
            PromjZLjU = mPBuff[globalIndex++]
            FlagLjVr = mPBuff[globalIndex++]
        }
    }

    private fun getUklsPar() {
        mUkls.BrProg = mPBuff[globalIndex++]
        mUkls.rel3p = mPBuff[globalIndex++]
        if (mUkls.BrProg <= 16) {
            val x = UniUksByt()
            for (i in 0 until mUkls.BrProg) {
                x.i = setOprel3I()
                x.updateP()
                mUkls.TPro[i] = x.p
            }

        }

    }

    private fun getPProg(rel: Int, mNProNum: Int): Opprog {
        return when (rel) {
            1 -> mPProgR1[mNProNum]
            2 -> mPProgR2[mNProNum]
            3 -> mPProgR3[mNProNum]
            4 -> mPProgR4[mNProNum]
            else -> mPProgR1[mNProNum]
        }
    }


    private fun fillTelegram(telegram: Telegram) {
        telegram.Cmd.fillTelegCMD()
    }

    private fun fillTelegramTlg(telegram: Tlg) {
        // this is union struct
        fillTelegram(telegram.tel1)
    }

    private fun fillTelegRel(telegram: Telegrel) {
        with(telegram) {
            Uk.fillTelegCMD()
            Isk.fillTelegCMD()
        }
    }

    private fun TelegCMD.fillTelegCMD() {
        for (i in 0 until 7)
            AktiImp[i] = mPBuff[globalIndex++]
        BrAkImp = mPBuff[globalIndex++]
        for (i in 0 until 7)
            NeutImp[i] = mPBuff[globalIndex++]
        Fn = mPBuff[globalIndex++]
    }

    private fun fillBuffer(address: String) {
        globalIndex = 0
        mPBuff = addressMap[address]
            ?: throw Exception(context.getString(R.string.address_does_not_exist))
    }

    private fun getPg2Par() {
        with(mOprij) {
            VOpRe.StaPrij = mPBuff[globalIndex++]
            VOpRe.VakProR1 = setOprelI()
            VOpRe.VakProR2 = setOprelI()
            VOpRe.VakProR3 = setOprelI()
            VOpRe.VakProR4 = setOprelI()

            VIdBr = mPBuff[globalIndex++]
            ParFlags = mPBuff[globalIndex++]

            StaR1PwON_OFF = mPBuff[globalIndex++]
            StaR2PwON_OFF = mPBuff[globalIndex++]
            StaR3PwON_OFF = mPBuff[globalIndex++]
            StaR4PwON_OFF = mPBuff[globalIndex++]

            if (mSoftwareVersion >= 57) {
                VCRel1Tu = mPBuff[globalIndex++]
                VCRel2Tu = mPBuff[globalIndex++]
                VCRel3Tu = mPBuff[globalIndex++]
                VCRel4Tu = mPBuff[globalIndex++]
            }
        }

        if (mSoftwareVersion >= 82) {
            for (i in 0 until 4) {
                mRealloc[i].rel_on = mPBuff[globalIndex++]
                mRealloc[i].rel_off = mPBuff[globalIndex++]
            }
        }

        if (mHardwareVersion == TIP_PASN) {
            with(mOprij) {
                StaAsat = mPBuff[globalIndex++]
                AsatKorOn = mPBuff[globalIndex++]
                AsatKorOff = mPBuff[globalIndex++]
                PromjZLjU = mPBuff[globalIndex++]
                FlagLjVr = mPBuff[globalIndex++]
            }
        }
    }

    private fun getVerAdrParVer6() {
        setVerAdrData(mOprij.VAdrR1)
        setVerAdrData(mOprij.VAdrR2)
        setVerAdrData(mOprij.VAdrR3)
        setVerAdrData(mOprij.VAdrR4)


        mOprij.VC1R1 = setOprelI()
        mOprij.VC1R2 = setOprelI()
        mOprij.VC1R3 = setOprelI()
        mOprij.VC1R4 = setOprelI()

        for (i in 0 until 4)
            mOprij.CRelXSw[i] = mPBuff[globalIndex++]
    }

    private fun setVerAdrData(vadrr: Vadrr) {
        var adrxx: Byte = 0
        var bitadrxx: Byte = 0
        adrxx = mPBuff[globalIndex++]
        vadrr.VAdrRA = if (adrxx == 0.toByte()) 0 else getAdrNr(adrxx)

        adrxx = mPBuff[globalIndex++]
        bitadrxx = mPBuff[globalIndex++]
        bitadrxx = getAdrNr(bitadrxx)
        vadrr.VAdrRB =
            if ((adrxx.compare(0x80) == 0) || bitadrxx == 0.toByte()) 0 else (adrxx * 8 + bitadrxx).toByte()
        globalIndex += 4

        adrxx = mPBuff[globalIndex++]
        bitadrxx = mPBuff[globalIndex++]
        bitadrxx = getAdrNr(bitadrxx)
        vadrr.VAdrRC =
            if ((adrxx.compare(0x80) == 0) || bitadrxx == 0.toByte()) 0 else (adrxx * 8 + bitadrxx).toByte()

        adrxx = mPBuff[globalIndex++]
        bitadrxx = mPBuff[globalIndex++]
        bitadrxx = getAdrNr(bitadrxx)
        vadrr.VAdrRD =
            if ((adrxx.compare(0x80) == 0) || bitadrxx == 0.toByte()) 0 else (adrxx * 8 + bitadrxx).toByte()
        println()

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

    private fun getKlDatVer9file() {
        getKlDatVer6()
        for (i in 1 until 13)
            mOp50rij.SinhTime[i] = setOprel4I()
        mOp50rij.RTCSinh = mPBuff[globalIndex++]
        mOp50rij.WDaySinh = mPBuff[globalIndex++]
        mOp50rij.CPWBRTIME = setOprelI()
        mOp50rij.CLOGENFLGS[0] = setOprelI().toShort()
        mOp50rij.CLOGENFLGS[1] = setOprelI().toShort()
        mOp50rij.CLOGENFLGS[2] = setOprelI().toShort()
    }

    private fun getKlDatVer6() {
        mOprij.VDuzAdr = mPBuff[globalIndex++]
        mOprij.KlOpR1 = setDlyRelData()
        mOprij.KlOpR2 = setDlyRelData()
        mOprij.KlOpR3 = setDlyRelData()
        mOprij.KlOpR4 = setDlyRelData()
        mOprij.Dly24H = setOprelI()
        mOprij.PolUKRe = mPBuff[globalIndex++]
        if (mSoftwareVersion >= 98)
            mOp50rij.SinhTime[0] = setOprel4I()


    }

    private fun setDlyRelData(): Klopr {
        return Klopr().apply {
            KRelDela = setOprel3I()
            KRelDelb = setOprel3I()
        }
    }

    private fun setOprelI(): Int {
        val b1 = mPBuff[globalIndex++]
        val b0 = mPBuff[globalIndex++]
        val tempi = Uni4byt(byteArrayOf(b0, b1, 0, 0))
        return tempi.i
    }

    private fun setOprel3I(): Int {
        val b2 = mPBuff[globalIndex++]
        val b1 = mPBuff[globalIndex++]
        val b0 = mPBuff[globalIndex++]
        val tempi = Uni4byt(byteArrayOf(b0, b1, b2, 0))
        return tempi.i
    }

    private fun setOprel4I(): Int {
        val b3 = mPBuff[globalIndex++]
        val b2 = mPBuff[globalIndex++]
        val b1 = mPBuff[globalIndex++]
        val b0 = mPBuff[globalIndex++]
        val tempi = Uni4byt(byteArrayOf(b0, b1, b2, b3))
        return tempi.i
    }


    private fun fillAddressMap(lines: List<String>) {
        val a = ','.toByte()
        for (line in lines) {
            println(line)
            val addressData = line.split("(")
            val address = addressData.getOrElse(0) { "" }
            val data = addressData.getOrElse(1) { "" }
                .replace(")", "")
                .chunked(2)
                .map {
                    if (it.contains(",")) {
                        var value = 0
                        for ((i, c) in it.withIndex()) {
                            if (c == ',') {
                                value += (192 * 10.0.pow(i)).toInt()
                            } else
                                value += ((c - '0') * 10.0.pow(i)).toInt()
                        }
                        return@map (value and 0xFF).toByte()


                    } else
                        return@map (it.toInt(16) and 0xFF).toByte()
                }
                .toByteArray()
            addressMap[address] = data
            println()
        }
    }

    private fun extractComment(line: String?) {
        if (line != null) {
            mFileComment = line.split("\\s+".toRegex()).getOrElse(1, defaultValue = { "" })
        }
    }

    private fun extractConstants(line: String?) {
        if (line != null) {
            val chars = line.split("#").filter { it.isNotEmpty() }.map { removeNonAlphanumeric(it) }
            val FR = getInt(chars[0])
            mParFiltera.BROJ = FR
            mParFilteraCF.BROJ = FR
            if (mSoftwareVersion >= 80) {
                val ptabpar = DataUtils.getTbparfiltera98mhz()[FR - 1]
                mParFiltera.UTHMIN = ptabpar.UTHMIN
                mParFiltera.UTLMAX = ptabpar.UTLMAX
            } else throw Error(context.getString(R.string.wrong_software_version))

            val RA = getInt(chars[1])
            mBrojRast = RA

            if (chars[2].contains("#UF")) {
                val UF = getInt(chars[2])
                mUtfPosto = UF / 100.0
            } else mUtfPosto = 0.5

            val DP = getInt(chars[3])
            mCfg.cID = DP
        }
    }

    private fun extractDeviceInfo(line: String?) {
        if (line != null) {
            if (line.startsWith("UPMTK")) {
                val chars = line.split("-")
                var ch = chars[1].toInt()
                if (ch in 0..2)
                    mTip = ch - 1
                ch = chars[3].toInt()
                if (ch >= 0)
                    mHardwareVersion = ch - 1
                mSoftwareVersion = chars[5].toInt()
            }
        }
    }

    private fun getInt(input: String): Int {
        var inputFormatted = ""
        if (input.length > 2)
            inputFormatted = input.substring(2)
        if (isHexadecimal(inputFormatted))
            return Long.parseLong(inputFormatted, 16).toInt()
        return 0
    }
}