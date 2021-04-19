package com.mtkreader.services

import android.content.Context
import android.widget.Toast
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.Const.Data.TIP_PASN
import com.mtkreader.compare
import com.mtkreader.contracts.ParamsWriteContract
import com.mtkreader.data.DataStructures
import com.mtkreader.data.reading.*
import com.mtkreader.getBytes
import com.mtkreader.toPositiveInt
import com.mtkreader.utils.DataUtils
import com.mtkreader.utils.DataUtils.isHexadecimal
import com.mtkreader.utils.DataUtils.removeNonAlphanumeric
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.Long
import kotlin.math.pow

class ParamsWriteFillDataStructuresService : ParamsWriteContract.FillDataStructuresService, KoinComponent {

    private val context: Context by inject()
    private val data = DataStructures()
    private val addressMap = mutableMapOf<String, ByteArray>()

    companion object {
        private const val FILE_TOKEN = "//Programiranje"
        private const val KALBLOCK_SIZE = 0x30
        private const val NR_KAL_IN_BLOCK = KALBLOCK_SIZE / 8

    }

    override fun extractFileData(fileLines: List<String>): Single<DataStructures> {
        return Single.fromCallable { extract(fileLines) }
    }

    private fun extract(fileLines: List<String>): DataStructures {
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
        if (data.mSoftwareVersion >= 80)
            getKlDatVer9file()



        fillBuffer("8180")
        getVerAdrParVer6()

        fillBuffer("8280")
        getPg2Par()

        fillBuffer("9080")
        var numTelegrams = data.mPBuff.size / data.mOp50rij.TlgRel1.getBytes().size
        when (numTelegrams) {
            1 -> fillTelegRel(data.mOp50rij.TlgRel1)
            2 -> {
                fillTelegRel(data.mOp50rij.TlgRel1)
                fillTelegRel(data.mOp50rij.TlgRel2)
            }
            3 -> {
                fillTelegRel(data.mOp50rij.TlgRel1)
                fillTelegRel(data.mOp50rij.TlgRel2)
                fillTelegRel(data.mOp50rij.TlgRel3)
            }
        }

        fillBuffer("9180")
        numTelegrams = data.mPBuff.size / data.mOp50rij.TlgRel1.getBytes().size
        when (numTelegrams) {
            1 -> fillTelegRel(data.mOp50rij.TlgRel2)
            2 -> {
                fillTelegRel(data.mOp50rij.TlgRel2)
                fillTelegRel(data.mOp50rij.TlgRel3)
            }
            3 -> {
                fillTelegRel(data.mOp50rij.TlgRel2)
                fillTelegRel(data.mOp50rij.TlgRel3)
                fillTelegRel(data.mOp50rij.TlgRel4)
            }
        }

        fillBuffer("9280")
        numTelegrams = data.mPBuff.size / data.mOp50rij.TlgRel1.getBytes().size
        when (numTelegrams) {
            1 -> fillTelegRel(data.mOp50rij.TlgRel3)
            2 -> {
                fillTelegRel(data.mOp50rij.TlgRel3)
                fillTelegRel(data.mOp50rij.TlgRel4)
            }
            3 -> {
                fillTelegRel(data.mOp50rij.TlgRel3)
                fillTelegRel(data.mOp50rij.TlgRel4)
                fillTelegramTlg(data.mOp50rij.tlg[0])
            }
        }

        fillBuffer("9380")
        numTelegrams = data.mPBuff.size / data.mOp50rij.TlgRel1.getBytes().size
        when (numTelegrams) {
            1 -> fillTelegRel(data.mOp50rij.TlgRel4)
            2 -> {
                fillTelegRel(data.mOp50rij.TlgRel4)
                fillTelegramTlg(data.mOp50rij.tlg[0])
            }
            3 -> {
                fillTelegRel(data.mOp50rij.TlgRel4)
                fillTelegramTlg(data.mOp50rij.tlg[0])
                fillTelegramTlg(data.mOp50rij.tlg[1])
            }
        }

        fillBuffer("9480")
        fillTelegramTlgGroup(data.mOp50rij.tlg, 0)

        fillBuffer("9580")
        fillTelegramTlgGroup(data.mOp50rij.tlg, 3)

        fillBuffer("9680")
        fillTelegramTlgGroup(data.mOp50rij.tlg, 5)

        fillBuffer("9880")
        fillTelegramGroup(data.mTelegSync.toList(), 0)

        fillBuffer("9980")
        fillTelegramGroup(data.mTelegSync.toList(), 2)

        fillBuffer("9A80")
        fillTelegramGroup(data.mTelegSync.toList(), 5)

        fillBuffer("9B80")
        fillTelegramGroup(data.mTelegSync.toList(), 8)

        fillBuffer("9C80")
        fillTelegramGroup(data.mTelegSync.toList(), 10)

        fillBuffer("0180")
        data.mOprij.VAdrPrij = setOprel3I()


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
                x.b[1] = data.mPBuff[data.globalIndex++]
                x.b[0] = data.mPBuff[data.globalIndex++]
                x.updateI()
                pPProg.AkTim = x.i and 0xFFFC
                pPProg.DanPr = data.mPBuff[data.globalIndex++]
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

        if (data.mSoftwareVersion >= 95) {
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

        if (data.mSoftwareVersion >= 80) {
            data.mBrUpKalendara = 0
            fillBuffer("A080")
            getBrUpKalendara()
            try {
                for (i in 0 until NR_KAL_IN_BLOCK)
                    fillKalendar(data.mKalendar[i])
            } catch (ex: ArrayIndexOutOfBoundsException) {
                // failed to mem cpy
            }
            if (data.mBrUpKalendara > 5) {
                val sizeOfStKalend = 8
                var nrBlok: Int =
                    (data.mBrUpKalendara - 5) / (sizeOfStKalend / (NR_KAL_IN_BLOCK - 1))
                if ((data.mBrUpKalendara - 5) / (sizeOfStKalend / (NR_KAL_IN_BLOCK - 1)) != 0) nrBlok++

                var cnt = 1
                do {
                    val address = String.format("A%01X80", cnt)
                    fillBuffer(address)
                    rel = (cnt * NR_KAL_IN_BLOCK) - 1
                    fillKalendar(data.mKalendar[rel])
                    cnt++
                } while (cnt < nrBlok)
            }
        }

        if (data.mSoftwareVersion >= 96) {
            fillBuffer("G6")
            getAfterProgSetting(true)

            fillBuffer("G7")
            readCreatedId(true)
        }

        return data
    }

    private fun readCreatedId(buff: Boolean) {
        if (!buff) return

        for (i in 0 until RecFilParStr.PARID_SIZE)
            data.mCFileParData.CreateSite[i] = data.mPBuff[data.globalIndex++]

        for (i in 0 until RecFilParStr.PARID_SIZE)
            data.mCFileParData.IDCreate[i] = data.mPBuff[data.globalIndex++]

        for (i in 0 until RecFilParStr.PARIDFILE_SIZE)
            data.mCFileParData.IDFile[i] = data.mPBuff[data.globalIndex++]

    }

    private fun getAfterProgSetting(buff: Boolean) {
        if (buff) {
            data.mInitRelSetProg.setPar = data.mPBuff[data.globalIndex++]
            for (i in 0 until 4)
                data.mInitRelSetProg.status[i] = data.mPBuff[data.globalIndex++]
        }
    }


    private fun fillKalendar(kalendar: StKalend) {
        with(kalendar) {
            broj = data.mPBuff[data.globalIndex++]
            danuTj = data.mPBuff[data.globalIndex++]
            akPr1do8 = data.mPBuff[data.globalIndex++]
            akPr9do15 = data.mPBuff[data.globalIndex++]
            fillDatum(PocDan)
        }
    }

    private fun fillDatum(datum: Datum) {
        datum.mje = data.mPBuff[data.globalIndex++]
        datum.dan = data.mPBuff[data.globalIndex++]
    }

    private fun getBrUpKalendara() {
        data.globalIndex++
        data.globalIndex++
        data.globalIndex++
        data.globalIndex++
        data.globalIndex++
        data.globalIndex++
        data.globalIndex++
        data.mBrUpKalendara = data.mPBuff[data.globalIndex++]
    }

    private fun getFriRParVer9() {
        with(data.mParFilteraCF) {
            NYM1 = data.mPBuff[data.globalIndex++]
            NYM2 = data.mPBuff[data.globalIndex++]
            K_V = setOprelI()
            REZ = setOprelI()
            UTHMIN = setOprelI()
            UTLMAX = setOprelI()
            PERIOD = setOprelI()
            FORMAT = setOprelI()
            BROJ = data.mPBuff[data.globalIndex++].toInt()
            data.globalIndex++
            data.mBrojRast = setOprelI()
            setUfPosto()
        }
    }

    private fun setUfPosto() {
        val broj = data.mParFilteraCF.BROJ
        if (broj >= 0) {
            var KvUt = data.mParFilteraCF.K_V
            if (KvUt == 0) {
                KvUt = DataUtils.getTbParFilteraVer9()[broj].K_V
                Toast.makeText(context, "Error 'Kv = 0' ", Toast.LENGTH_SHORT).show()
            }

            val utth = data.mParFilteraCF.UTHMIN.toDouble()
            var uthMin = utth / KvUt.toDouble()

            uthMin *= 1.002
            var utlMax = data.mParFilteraCF.UTLMAX.toDouble() / KvUt.toDouble()
            utlMax *= 1.002

            var uthMinRef = 0.0
            val ith = getFrUTHDefVer9(broj)
            if (ith != 0) {
                uthMinRef = ith.toDouble()
                data.mUtfPosto = (utth * data.UTFREFP) / uthMinRef
            } else
                data.mUtfPosto = 0.0
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
            with(data.mRelInterlock[i]) {
                wBitsOn = setOprelI()
                wBitsOff = setOprelI()
                PcCnfg[0] = setOprelI()
                PcCnfg[1] = setOprelI()
            }
        }
    }

    private fun getStrLoadMng() {
        for (i in 0 until 4) {
            with(data.mLearningRx[i]) {
                Status = data.mPBuff[data.globalIndex++]
                relPos = data.mPBuff[data.globalIndex++]
                TPosMin = setOprel3I()
                TPosMax = setOprel3I()
            }
        }
    }

    private fun getTlgAbsenceDat() {
        for (i in 0 until 4) {
            with(data.mTelegAbsenceRx[i]) {
                OnRes = data.mPBuff[data.globalIndex++]
                TDetect = setOprel3I()
                RestOn = data.mPBuff[data.globalIndex++]
                OnTaExe = data.mPBuff[data.globalIndex++]
            }
        }
    }

    private fun getPonPoffRDat() {
        setPonPoffReData(data.mPonPoffRx[0])
        setPonPoffReData(data.mPonPoffRx[1])
        setPonPoffReData(data.mPonPoffRx[2])
        setPonPoffReData(data.mPonPoffRx[3])
    }

    private fun setPonPoffReData(ponPoffStr: PonPoffStr) {
        with(ponPoffStr) {
            OnPonExe = data.mPBuff[data.globalIndex++]
            lperIgno = data.mPBuff[data.globalIndex++]
            TminSwdly = setOprel3I()
            TrndSwdly = setOprel3I()
            Tlng = setOprel3I()

            if ((Tlng and 0x800000) != 0)
                lperIgno = 0x80.toByte()
            else
                lperIgno = 0x00.toByte()

            Tlng = Tlng and 0x7fffff
            lOnPonExe = data.mPBuff[data.globalIndex++]
            OnPoffExe = data.mPBuff[data.globalIndex++]
            TBlockPrePro = setOprel3I()
        }

    }


    private fun getWiperData() {
        for (i in 0 until 4) {
            data.mWipersRx[i].status = (0x80 + 0x20).toByte()
            setWiperRelData(data.mWipersRx[i])
        }
    }

    private fun setWiperRelData(wiper: Wiper) {
        wiper.status = data.mPBuff[data.globalIndex++]
        wiper.Tswdly = setOprel3I()
        wiper.TWiper = setOprel3I()
        wiper.TBlockPrePro = setOprel3I()
    }

    private fun getPrazDaniPar() {
        var brpraz: Int = data.mPBuff[data.globalIndex++].toPositiveInt()
        brpraz = brpraz shl 8
        brpraz = brpraz or data.mPBuff[data.globalIndex++].toPositiveInt()
        data.mPraznici.brpraz = brpraz.toShort()
        if (brpraz <= 16) {
            for (i in 0 until brpraz) {
                data.mPraznici.datum[i].mje = data.mPBuff[data.globalIndex++]
                data.mPraznici.datum[i].dan = data.mPBuff[data.globalIndex++]
            }
        }

    }

    private fun getAsatPar() {
        with(data.mOprij) {
            StaAsat = data.mPBuff[data.globalIndex++]
            AsatKorOn = data.mPBuff[data.globalIndex++]
            AsatKorOff = data.mPBuff[data.globalIndex++]
            PromjZLjU = data.mPBuff[data.globalIndex++]
            FlagLjVr = data.mPBuff[data.globalIndex++]
        }
    }

    private fun getUklsPar() {
        data.mUkls.BrProg = data.mPBuff[data.globalIndex++]
        data.mUkls.rel3p = data.mPBuff[data.globalIndex++]
        if (data.mUkls.BrProg <= 16) {
            val x = UniUksByt()
            for (i in 0 until data.mUkls.BrProg) {
                x.i = setOprel3I()
                x.updateP()
                data.mUkls.TPro[i] = x.p
            }

        }

    }

    private fun getPProg(rel: Int, mNProNum: Int): Opprog {
        return when (rel) {
            1 -> data.mPProgR1[mNProNum]
            2 -> data.mPProgR2[mNProNum]
            3 -> data.mPProgR3[mNProNum]
            4 -> data.mPProgR4[mNProNum]
            else -> data.mPProgR1[mNProNum]
        }
    }


    private fun fillTelegramGroup(telegrams: List<Telegram>, startIndex: Int) {
        val numTelegrams = data.mPBuff.size / telegrams[startIndex].getBytes().size
        for (i in startIndex until startIndex + numTelegrams)
            fillTelegram(telegrams[i])
    }

    private fun fillTelegram(telegram: Telegram) {
        telegram.Cmd.fillTelegCMD()
    }

    private fun fillTelegramTlgGroup(tlgs: List<Tlg>, startIndex: Int) {
        val numTelegrams = data.mPBuff.size / tlgs[startIndex].tel1.getBytes().size
        for (i in startIndex until startIndex + numTelegrams)
            fillTelegramTlg(tlgs[i])
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
            AktiImp[i] = data.mPBuff[data.globalIndex++]
        BrAkImp = data.mPBuff[data.globalIndex++]
        for (i in 0 until 7)
            NeutImp[i] = data.mPBuff[data.globalIndex++]
        Fn = data.mPBuff[data.globalIndex++]
    }

    private fun fillBuffer(address: String) {
        data.globalIndex = 0
        data.mPBuff = addressMap[address]
            ?: throw Exception(context.getString(R.string.address_does_not_exist))
    }

    private fun getPg2Par() {
        with(data.mOprij) {
            VOpRe.StaPrij = data.mPBuff[data.globalIndex++]
            VOpRe.VakProR1 = setOprelI()
            VOpRe.VakProR2 = setOprelI()
            VOpRe.VakProR3 = setOprelI()
            VOpRe.VakProR4 = setOprelI()

            VIdBr = data.mPBuff[data.globalIndex++]
            ParFlags = data.mPBuff[data.globalIndex++]

            StaR1PwON_OFF = data.mPBuff[data.globalIndex++]
            StaR2PwON_OFF = data.mPBuff[data.globalIndex++]
            StaR3PwON_OFF = data.mPBuff[data.globalIndex++]
            StaR4PwON_OFF = data.mPBuff[data.globalIndex++]

            if (data.mSoftwareVersion >= 57) {
                VCRel1Tu = data.mPBuff[data.globalIndex++]
                VCRel2Tu = data.mPBuff[data.globalIndex++]
                VCRel3Tu = data.mPBuff[data.globalIndex++]
                VCRel4Tu = data.mPBuff[data.globalIndex++]
            }
        }

        if (data.mSoftwareVersion >= 82) {
            for (i in 0 until 4) {
                data.mRealloc[i].rel_on = data.mPBuff[data.globalIndex++]
                data.mRealloc[i].rel_off = data.mPBuff[data.globalIndex++]
            }
        }

        if (data.mHardwareVersion == TIP_PASN) {
            with(data.mOprij) {
                StaAsat = data.mPBuff[data.globalIndex++]
                AsatKorOn = data.mPBuff[data.globalIndex++]
                AsatKorOff = data.mPBuff[data.globalIndex++]
                PromjZLjU = data.mPBuff[data.globalIndex++]
                FlagLjVr = data.mPBuff[data.globalIndex++]
            }
        }
    }

    private fun getVerAdrParVer6() {
        setVerAdrData(data.mOprij.VAdrR1)
        setVerAdrData(data.mOprij.VAdrR2)
        setVerAdrData(data.mOprij.VAdrR3)
        setVerAdrData(data.mOprij.VAdrR4)


        data.mOprij.VC1R1 = setOprelI()
        data.mOprij.VC1R2 = setOprelI()
        data.mOprij.VC1R3 = setOprelI()
        data.mOprij.VC1R4 = setOprelI()

        for (i in 0 until 4)
            data.mOprij.CRelXSw[i] = data.mPBuff[data.globalIndex++]
    }

    private fun setVerAdrData(vadrr: Vadrr) {
        var adrxx: Byte = 0
        var bitadrxx: Byte = 0
        adrxx = data.mPBuff[data.globalIndex++]
        vadrr.VAdrRA = if (adrxx == 0.toByte()) 0 else getAdrNr(adrxx)

        adrxx = data.mPBuff[data.globalIndex++]
        bitadrxx = data.mPBuff[data.globalIndex++]
        bitadrxx = getAdrNr(bitadrxx)
        vadrr.VAdrRB =
            if ((adrxx.compare(0x80) == 0) || bitadrxx == 0.toByte()) 0 else (adrxx * 8 + bitadrxx).toByte()
        data.globalIndex += 4

        adrxx = data.mPBuff[data.globalIndex++]
        bitadrxx = data.mPBuff[data.globalIndex++]
        bitadrxx = getAdrNr(bitadrxx)
        vadrr.VAdrRC =
            if ((adrxx.compare(0x80) == 0) || bitadrxx == 0.toByte()) 0 else (adrxx * 8 + bitadrxx).toByte()

        adrxx = data.mPBuff[data.globalIndex++]
        bitadrxx = data.mPBuff[data.globalIndex++]
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
            data.mOp50rij.SinhTime[i] = setOprel4I()
        data.mOp50rij.RTCSinh = data.mPBuff[data.globalIndex++]
        data.mOp50rij.WDaySinh = data.mPBuff[data.globalIndex++]
        data.mOp50rij.CPWBRTIME = setOprelI()
        data.mOp50rij.CLOGENFLGS[0] = setOprelI().toShort()
        data.mOp50rij.CLOGENFLGS[1] = setOprelI().toShort()
        data.mOp50rij.CLOGENFLGS[2] = setOprelI().toShort()
    }

    private fun getKlDatVer6() {
        data.mOprij.VDuzAdr = data.mPBuff[data.globalIndex++]
        data.mOprij.KlOpR1 = setDlyRelData()
        data.mOprij.KlOpR2 = setDlyRelData()
        data.mOprij.KlOpR3 = setDlyRelData()
        data.mOprij.KlOpR4 = setDlyRelData()
        data.mOprij.Dly24H = setOprelI()
        data.mOprij.PolUKRe = data.mPBuff[data.globalIndex++]
        if (data.mSoftwareVersion >= 98)
            data.mOp50rij.SinhTime[0] = setOprel4I()


    }

    private fun setDlyRelData(): Klopr {
        return Klopr().apply {
            KRelDela = setOprel3I()
            KRelDelb = setOprel3I()
        }
    }

    private fun setOprelI(): Int {
        val b1 = data.mPBuff[data.globalIndex++]
        val b0 = data.mPBuff[data.globalIndex++]
        val tempi = Uni4byt(byteArrayOf(b0, b1, 0, 0))
        return tempi.i
    }

    private fun setOprel3I(): Int {
        val b2 = data.mPBuff[data.globalIndex++]
        val b1 = data.mPBuff[data.globalIndex++]
        val b0 = data.mPBuff[data.globalIndex++]
        val tempi = Uni4byt(byteArrayOf(b0, b1, b2, 0))
        return tempi.i
    }

    private fun setOprel4I(): Int {
        val b3 = data.mPBuff[data.globalIndex++]
        val b2 = data.mPBuff[data.globalIndex++]
        val b1 = data.mPBuff[data.globalIndex++]
        val b0 = data.mPBuff[data.globalIndex++]
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
            data.mFileComment = line.split("\\s+".toRegex()).getOrElse(1, defaultValue = { "" })
        }
    }

    private fun extractConstants(line: String?) {
        if (line != null) {
            val chars =
                line.split("#").filter { it.isNotEmpty() }.map { removeNonAlphanumeric(it) }
            val FR = getInt(chars[0])
            data.mParFiltera.BROJ = FR
            data.mParFilteraCF.BROJ = FR
            if (data.mSoftwareVersion >= 80) {
                val ptabpar = DataUtils.getTbparfiltera98mhz()[FR - 1]
                data.mParFiltera.UTHMIN = ptabpar.UTHMIN
                data.mParFiltera.UTLMAX = ptabpar.UTLMAX
            } else throw Error(context.getString(R.string.wrong_software_version))

            val RA = getInt(chars[1])
            data.mBrojRast = RA

            if (chars[2].contains("#UF")) {
                val UF = getInt(chars[2])
                data.mUtfPosto = UF / 100.0
            } else data.mUtfPosto = 0.5

            val DP = getInt(chars[3])
            data.mCfg.cID = DP
        }
    }

    private fun extractDeviceInfo(line: String?) {
        if (line != null) {
            if (line.startsWith("UPMTK")) {
                val chars = line.split("-")
                var ch = chars[1].toInt()
                if (ch in 0..2)
                    data.mTip = ch - 1
                ch = chars[3].toInt()
                if (ch >= 0)
                    data.mHardwareVersion = ch - 1
                data.mSoftwareVersion = chars[5].toInt()
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