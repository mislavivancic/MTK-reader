package com.mtkreader.services

import com.mtkreader.commons.Const
import com.mtkreader.contracts.ParamsWriteContract
import com.mtkreader.data.DataStructures
import com.mtkreader.data.SendData
import com.mtkreader.data.reading.*
import com.mtkreader.data.writing.DataRXMessage
import com.mtkreader.data.writing.DataTXMessage
import com.mtkreader.exceptions.VerificationException
import com.mtkreader.getBytes
import com.mtkreader.trimAndSplit
import com.mtkreader.utils.DataUtils
import com.mtkreader.utils.DataUtils.byteArrayToHexString
import com.mtkreader.utils.DataUtils.hexToAscii
import com.mtkreader.utils.DataUtils.removeNonAlphanumeric
import io.reactivex.Single
import java.util.*
import kotlin.experimental.inv
import kotlin.experimental.xor

class WriteDataService : ParamsWriteContract.WriteDataService {

    private lateinit var data: DataStructures
    private val dataToWrite = mutableListOf<SendData>()
    private val imageWrite = mutableMapOf<String, String>()
    private val imageRead = mutableMapOf<String, String>()

    override fun generateStrings(data: DataStructures): Single<List<SendData>> {
        this.data = data
        return Single.fromCallable { setData() }
    }

    private fun setData(): List<SendData> {
        dataToWrite.clear()
        imageWrite.clear()

        var adrstr = "C080"
        var cmdstr = "W3"
        var datstr = setFrRaParVer9(false)
        setFrRaParVer9(false)
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("C080", datstr)

        //TELEGRAMI
        adrstr = ""
        cmdstr = "W3"
        datstr = setTlgData(0, 2)
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("9080", datstr)

        adrstr = ""
        cmdstr = "W3"
        datstr = setTlgData(1, 2)
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("9180", datstr)

        adrstr = ""
        cmdstr = "W3"
        datstr = setTlgData(2, 2)
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("9280", datstr)

        adrstr = ""
        cmdstr = "W3"
        datstr = setTlgData(3, 2)
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("9380", datstr)

        adrstr = ""
        cmdstr = "W3"
        datstr = setTlgData(4, 3)
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("9480", datstr)

        adrstr = ""
        cmdstr = "W3"
        datstr = setTlgData(5, 2)
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("9580", datstr)

        adrstr = ""
        cmdstr = "W3"
        datstr = setTlgData(6, 3)
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("9680", datstr)


        adrstr = ""
        cmdstr = "W3"
        datstr = SetKlDatVerPS981()
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("8080", datstr)

        adrstr = ""
        cmdstr = "W3"
        datstr = SetKl2VerDatVerPS981()
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("8180", datstr)

        adrstr = ""
        cmdstr = "W3"
        datstr = SetWiperDatVer95()
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("5080", datstr)

        adrstr = ""
        cmdstr = "W3"
        datstr = SetPonPoffRDatVer95()
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("5180", datstr)

        adrstr = ""
        cmdstr = "W3"
        datstr = SetTlgAbsensceDatVer95()
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("5280", datstr)

        adrstr = ""
        cmdstr = "W3"
        datstr = SetLearningDatVer95()
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("5380", datstr)


        adrstr = ""
        cmdstr = "W3"
        datstr = setTlgData(8, 2)
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("9880", datstr)

        adrstr = ""
        cmdstr = "W3"
        datstr = setTlgData(9, 3)
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("9980", datstr)

        adrstr = ""
        cmdstr = "W3"
        datstr = setTlgData(0x0A, 3)
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("9A80", datstr)

        adrstr = ""
        cmdstr = "W3"
        datstr = setTlgData(0x0B, 2)
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("9B80", datstr)

        adrstr = ""
        cmdstr = "W3"
        datstr = setTlgData(0x0C, 3)
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("9C80", datstr)


        adrstr = ""
        cmdstr = "W3"
        datstr = SetRelInterLock()
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("0380", datstr)

        adrstr = ""
        cmdstr = "W3"
        datstr = SetNewRecParData()
        Snd_D_Chk(cmdstr, adrstr, datstr, 1)
        AddToImg("0280", datstr)


        data.mOprij.VAdrPrij = 0
        adrstr = ""
        cmdstr = "W3"
        datstr = SetIDParVer9()
        Snd_D_Chk(cmdstr, adrstr, datstr, 0)

        WrProgPgPS981()

        return dataToWrite.toList()
    }

    private fun setFrRaParVer9(bbdefault: Boolean): String {
        val broj = data.mParFilteraCF.BROJ
        if (broj >= 0) {
            val ptabpar = DataUtils.getTbParFilteraVer9()[broj]
            data.mParFilteraCF.NYM1 = ptabpar.NYM1
            data.mParFilteraCF.NYM2 = ptabpar.NYM2

            data.mParFilteraCF.K_V = ptabpar.K_V
            data.mParFilteraCF.REZ = ptabpar.REZ

            if (bbdefault) {
                data.mParFilteraCF.UTHMIN = ptabpar.UTHMIN
                data.mParFilteraCF.UTLMAX = ptabpar.UTLMAX
            }
            data.mParFilteraCF.PERIOD = ptabpar.PERIOD
            data.mParFilteraCF.FORMAT = ptabpar.FORMAT
            data.mParFilteraCF.BROJ = ptabpar.BROJ
        }


        val ttt = Uni4byt(data.mParFilteraCF.BROJ)
        var pchData = String.format(
            "%02X%02X%04X%04X%04X%04X%04X%04X%02X00",
            data.mParFilteraCF.NYM1, data.mParFilteraCF.NYM1,
            data.mParFilteraCF.K_V, data.mParFilteraCF.REZ,
            data.mParFilteraCF.UTHMIN, data.mParFilteraCF.UTLMAX,
            data.mParFilteraCF.PERIOD, data.mParFilteraCF.FORMAT, ttt.b[0]
        )

        var checkSum: Short = (data.mParFilteraCF.NYM1 + data.mParFilteraCF.NYM1).toShort()
        checkSum = (checkSum + (data.mParFilteraCF.K_V shr 8) + (data.mParFilteraCF.K_V and 0xFF)).toShort()
        checkSum = (checkSum + (data.mParFilteraCF.REZ shr 8) + (data.mParFilteraCF.REZ and 0xFF)).toShort()
        checkSum = (checkSum + (data.mParFilteraCF.UTHMIN shr 8) + (data.mParFilteraCF.UTHMIN and 0xFF)).toShort()
        checkSum = (checkSum + (data.mParFilteraCF.UTLMAX shr 8) + (data.mParFilteraCF.UTLMAX and 0xFF)).toShort()
        checkSum = (checkSum + (data.mParFilteraCF.PERIOD shr 8) + (data.mParFilteraCF.PERIOD and 0xFF)).toShort()
        checkSum = (checkSum + (data.mParFilteraCF.FORMAT shr 8) + (data.mParFilteraCF.FORMAT and 0xFF)).toShort()
        checkSum = (checkSum + ttt.b[0]).toShort()

        for (i in 0 until 17) {
            val wdat = DataUtils.getParRasTlgVer9()[data.mBrojRast][i]
            checkSum = (checkSum + ((wdat shr 8).toShort() + (wdat and 0xFF).toShort())).toShort()
            pchData += String.format("%04X", wdat)
        }

        val sChecksum = checkSum.inv()
        pchData += String.format("%04X", sChecksum)

        return pchData
    }

    private fun setTlgData(grupa: Int, nrTlg: Int): String {
        var pchData = ""
        val pbuf = mutableListOf<Byte>()
        val numToTake = nrTlg * 16  // 16 size of telegram
        when (grupa) {
            0 -> {
                when (nrTlg) {
                    1 -> pbuf.addAll(data.mOp50rij.TlgRel1.getBytes().toList())
                    2 -> {
                        pbuf.addAll(data.mOp50rij.TlgRel1.getBytes().toList())
                        pbuf.addAll(data.mOp50rij.TlgRel2.getBytes().toList())
                    }
                    3 -> {
                        pbuf.addAll(data.mOp50rij.TlgRel1.getBytes().toList())
                        pbuf.addAll(data.mOp50rij.TlgRel2.getBytes().toList())
                        pbuf.addAll(data.mOp50rij.TlgRel3.getBytes().toList())
                    }
                }
            }
            1 -> when (nrTlg) {
                1 -> pbuf.addAll(data.mOp50rij.TlgRel2.getBytes().toList())
                2 -> {
                    pbuf.addAll(data.mOp50rij.TlgRel2.getBytes().toList())
                    pbuf.addAll(data.mOp50rij.TlgRel3.getBytes().toList())
                }
                3 -> {
                    pbuf.addAll(data.mOp50rij.TlgRel2.getBytes().toList())
                    pbuf.addAll(data.mOp50rij.TlgRel3.getBytes().toList())
                    pbuf.addAll(data.mOp50rij.TlgRel4.getBytes().toList())
                }
            }
            2 -> when (nrTlg) {
                1 -> pbuf.addAll(data.mOp50rij.TlgRel3.getBytes().toList())
                2 -> {
                    pbuf.addAll(data.mOp50rij.TlgRel3.getBytes().toList())
                    pbuf.addAll(data.mOp50rij.TlgRel4.getBytes().toList())
                }
                3 -> {
                    pbuf.addAll(data.mOp50rij.TlgRel3.getBytes().toList())
                    pbuf.addAll(data.mOp50rij.TlgRel4.getBytes().toList())
                    pbuf.addAll(data.mOp50rij.tlg[0].tel1.getBytes().toList())
                }
            }
            3 -> when (nrTlg) {
                1 -> pbuf.addAll(data.mOp50rij.TlgRel4.getBytes().toList())
                2 -> {
                    pbuf.addAll(data.mOp50rij.TlgRel4.getBytes().toList())
                    pbuf.addAll(data.mOp50rij.tlg[0].tel1.getBytes().toList())
                }
                3 -> {
                    pbuf.addAll(data.mOp50rij.TlgRel4.getBytes().toList())
                    pbuf.addAll(data.mOp50rij.tlg[0].tel1.getBytes().toList())
                    pbuf.addAll(data.mOp50rij.tlg[1].tel1.getBytes().toList())
                }
            }
            4 -> pbuf.addAll(getTlgBytesGroup(data.mOp50rij.tlg, 0, nrTlg))
            5 -> pbuf.addAll(getTlgBytesGroup(data.mOp50rij.tlg, 3, nrTlg))
            6 -> pbuf.addAll(getTlgBytesGroup(data.mOp50rij.tlg, 5, nrTlg))
            8 -> pbuf.addAll(getTelegramBytesGroup(data.mTelegSync, 0, nrTlg))
            9 -> pbuf.addAll(getTelegramBytesGroup(data.mTelegSync, 2, nrTlg))
            0x0A -> pbuf.addAll(getTelegramBytesGroup(data.mTelegSync, 5, nrTlg))
            0x0B -> pbuf.addAll(getTelegramBytesGroup(data.mTelegSync, 8, nrTlg))
            0x0C -> pbuf.addAll(getTelegramBytesGroup(data.mTelegSync, 10, nrTlg))
        }

        for (byte in pbuf.take(numToTake)) {
            pchData += String.format("%02X", byte)
        }
        return pchData
    }

    private fun getTlgBytesGroup(tlgs: List<Tlg>, startIndex: Int, telegNum: Int): List<Byte> {
        val buf = mutableListOf<Byte>()
        for (i in startIndex until startIndex + telegNum)
            buf.addAll(tlgs[i].tel1.getBytes().toList())
        return buf
    }

    private fun getTelegramBytesGroup(telegrams: Array<Telegram>, startIndex: Int, telegNum: Int): List<Byte> {
        val buf = mutableListOf<Byte>()
        for (i in startIndex until startIndex + telegNum)
            buf.addAll(telegrams[i].getBytes().toList())
        return buf
    }

    private fun SetKlDatVerPS981(): String {
        var res: String = ""
        res += GetDlyRelDV9(data.mOprij.KlOpR1)
        res += GetDlyRelDV9(data.mOprij.KlOpR2)
        res += GetDlyRelDV9(data.mOprij.KlOpR3)
        res += GetDlyRelDV9(data.mOprij.KlOpR4)
        for (i in 0..3) {
            res += String.format("%02X%02X", data.mRealloc[i].rel_on, data.mRealloc[i].rel_off)
        }

        data.mOp50rij.CLOGENFLGS[2] = 0x0 // nekakva rezerva
        res += String.format(
            "%04X%04X%04X%04X",
            data.mOp50rij.CPWBRTIME,
            data.mOp50rij.CLOGENFLGS[0],
            data.mOp50rij.CLOGENFLGS[1],
            data.mOp50rij.CLOGENFLGS[2]
        )
        return res

    }

    private fun GetDlyRelDV9(Relx: Klopr): String {
        return String.format("%08X%08X", Relx.KRelDela, Relx.KRelDelb)
    }

    private fun SetKl2VerDatVerPS981(): String {
        var res: String = ""
        res += String.format("%02X%02X%02X%02X", data.mOprij.PolUKRe, data.mOp50rij.RTCSinh, data.mOprij.VOpRe.StaPrij, data.mOprij.PromjZLjU)
        for (i in 0..12)
            res += String.format("%08X", data.mOp50rij.SinhTime[i])
        return res
    }

    private fun SetWiperDatVer95(): String {
        var res: String = ""
        for (i in 0..3)
            res += GetWiperRelData(data.mWipersRx[i])
        return res
    }

    private fun GetWiperRelData(wipRelx: Wiper): String {
        return String.format("%02X%06X%06X%06X", wipRelx.status, wipRelx.Tswdly, wipRelx.TWiper, wipRelx.TBlockPrePro)
    }

    private fun SetPonPoffRDatVer95(): String {
        var res: String = ""
        for (i in 0..3) {
            var t3 = data.mPonPoffRx[i].Tlng.toInt()
            if (data.mPonPoffRx[i].lperIgno.toInt() != 0) t3 = t3 or 0x800000
            //var ign=data.mPonPoffRx[i].lperIgno //TODO check
            var ign = 0
            res += String.format(
                "%02X%02X%06X%06X%06X%02X%02X%06X",
                data.mPonPoffRx[i].OnPonExe,
                ign,
                data.mPonPoffRx[i].TminSwdly,
                data.mPonPoffRx[i].TrndSwdly,
                t3,
                data.mPonPoffRx[i].lOnPonExe,
                data.mPonPoffRx[i].OnPoffExe,
                data.mPonPoffRx[i].TBlockPrePro
            )

        }
        return res
    }


    private fun SetTlgAbsensceDatVer95(): String {
        var res: String = ""
        for (i in 0..3) {
            res += String.format(
                "%02X%06X%02X%02X",
                data.mTelegAbsenceRx[i].OnRes,
                data.mTelegAbsenceRx[i].TDetect,
                data.mTelegAbsenceRx[i].RestOn,
                data.mTelegAbsenceRx[i].OnTaExe
            )

        }
        return res
    }

    private fun SetLearningDatVer95(): String {
        var res: String = ""
        for (i in 0..3) {
            res += String.format(
                "%02X%02X%06X%06X",
                data.mLearningRx[i].Status,
                data.mLearningRx[i].relPos,
                data.mLearningRx[i].TPosMin,
                data.mLearningRx[i].TPosMax
            )

        }
        return res
    }


    private fun SetRelInterLock(): String {
        var res: String = ""
        for (i in 0..3) {
            res += String.format(
                "%04X%04X%04X%04X",
                data.mRelInterlock[i].wBitsOn,
                data.mRelInterlock[i].wBitsOff,
                data.mRelInterlock[i].PcCnfg[0],
                data.mRelInterlock[i].PcCnfg[1]
            )

        }
        return res
    }

    private fun toBCD(x: Int): Int {
        return ((x / 10) shl 4) or (x % 10)
    }

    private fun SetNewRecParData(): String {
        var res: String = ""
        val sek = Calendar.getInstance().get(Calendar.SECOND)
        val min = Calendar.getInstance().get(Calendar.MINUTE)
        val sat = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val dat = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val mje = Calendar.getInstance().get(Calendar.MONTH) + 1
        val god = Calendar.getInstance().get(Calendar.YEAR)

        data.m_cNewParData.DataTime[0] = toBCD(sek).toByte()
        data.m_cNewParData.DataTime[1] = toBCD(min).toByte()
        data.m_cNewParData.DataTime[2] = toBCD(sat).toByte()
        data.m_cNewParData.DataTime[3] = toBCD(dat).toByte()
        data.m_cNewParData.DataTime[4] = toBCD(mje).toByte()
        data.m_cNewParData.DataTime[5] = toBCD(god / 100).toByte()

        var IDRePara: String = "IDREPAR"
        IDRePara = "a"
        data.m_cNewParData.IDRePara = REC_PAR_STR.StringToByteArrTrimed(IDRePara, REC_PAR_STR.PARID_SIZE)
        var ReParaSite: String = "ReParaSite"
        ReParaSite = "Korisnik"
        data.m_cNewParData.ReParaSite = REC_PAR_STR.StringToByteArrTrimed(ReParaSite, REC_PAR_STR.PARID_SIZE)
        var IDCreate: String = "IDCreate"
        IDCreate = "a"
        data.m_cNewParData.IDCreate = REC_PAR_STR.StringToByteArrTrimed(IDCreate, REC_PAR_STR.PARID_SIZE)
        var CreateSite: String = "CreateSite"
        CreateSite = "Korisnik"
        data.m_cNewParData.CreateSite = REC_PAR_STR.StringToByteArrTrimed(CreateSite, REC_PAR_STR.PARID_SIZE)
        var IDFile: String = "IDFile135613316136136"
        IDFile = "ps"
        data.m_cNewParData.IDFile = REC_PAR_STR.StringToByteArrTrimed(IDFile, REC_PAR_STR.PARIDFILE_SIZE)

        for (i in 0 until REC_PAR_STR.DataTime_SIZE) res += String.format("%02X", data.m_cNewParData.DataTime[i])
        for (i in 0 until REC_PAR_STR.PARID_SIZE) res += String.format("%02X", data.m_cNewParData.CreateSite[i])
        for (i in 0 until REC_PAR_STR.PARID_SIZE) res += String.format("%02X", data.m_cNewParData.IDCreate[i])
        for (i in 0 until REC_PAR_STR.PARID_SIZE) res += String.format("%02X", data.m_cNewParData.ReParaSite[i])
        for (i in 0 until REC_PAR_STR.PARID_SIZE) res += String.format("%02X", data.m_cNewParData.IDRePara[i])
        for (i in 0 until REC_PAR_STR.PARIDFILE_SIZE) res += String.format("%02X", data.m_cNewParData.IDFile[i])

        return res
    }

    private fun SetIDParVer9(): String {
        var res = ""

        val buf = CharArray(10)
        var i = 0
        val sz = 10
        var V: Int = data.mOprij.VAdrPrij
        res = String.format("%08X", data.mOprij.VAdrPrij)

        if (V > 0) {
            i = sz
            while (V > 0) {
                i--
                buf[i] = (V % 10).toChar()
                V = V / 10
                if (i == 0) break
            }
            while (i < sz) {
                res += String.format("%02X", buf[i] + '0'.toInt())
                i++
            }
        }
        res += "0000000000000000000000000000000000000000"
        return res
    }

    private fun WrProgPgPS981() {
        var datstr: String
        var adrstr: String
        var cmdstr: String

        var rel: Int = 1

        cmdstr = "W2"
        adrstr = String.format("10%02X", rel)
        datstr = String.format("FFFF")
        Snd_D_Chk(cmdstr, adrstr, datstr, 0)

        cmdstr = "W3"
        adrstr = String.format("8280")
        datstr = SetAkProgID100()
        Snd_D_Chk(cmdstr, adrstr, datstr, 0)
        AddToImg("8280", datstr)


        //upis  programa koji nisu prazni
        var len: Int = 35
        var NrTpar = data.mCfg.cNpar
        NrTpar = 11 // TODO(remove hardcoding)
        var brProg = data.mCfg.cNprog
        brProg = 9 // TODO(remove hardcoding)
        var brRel = data.mCfg.cNrel + 1
        brRel = 3 + 1 // TODO(remove hardcoding)
        len = 35
        rel = 1
        var nProNum = 0

        do {
            nProNum = 0
            do {
                cmdstr = "W3"
                datstr = SetProgDat(rel, nProNum, len, NrTpar.toInt())
                if (datstr != "") {
                    adrstr = String.format("%01X%01X80", rel, nProNum)
                    Snd_D_Chk(cmdstr, adrstr, datstr, 0)
                    AddToImg(adrstr, datstr)

                }
                nProNum++
            } while (nProNum < brProg)//16

            rel++
        } while (rel < brRel)

    }

    private fun SetAkProgID100(): String {
        var res: String = ""

        res += String.format(
            "%04X%04X%04X%04X%02X%02X",
            data.mOprij.VOpRe.VakProR1,
            data.mOprij.VOpRe.VakProR2,
            data.mOprij.VOpRe.VakProR3,
            data.mOprij.VOpRe.VakProR4,
            data.mOprij.VOpRe.StaPrij,
            data.mOprij.ParFlags
        )

        return res
    }

    private fun SetProgDat(rel: Int, nProNum: Int, len: Int, NrTpar: Int): String {
        var res: String = ""
        var ncount = 0
        var rval = true
        var pPProg = Opprog()
        var x: Unitimbyt = Unitimbyt()
        when (rel) {
            1 -> pPProg = data.mPProgR1[nProNum]
            2 -> pPProg = data.mPProgR2[nProNum]
            3 -> pPProg = data.mPProgR3[nProNum]
            4 -> pPProg = data.mPProgR4[nProNum]
        }
        if (pPProg.AkTim == 0) return ""
        res = String.format("%04X%02X", pPProg.AkTim, pPProg.DanPr)
        ncount = 2
        for (i in 0 until NrTpar) {
            res += String.format("%06X", pPProg.TPro[i].I)
            ncount += 3
        }


        if (ncount < len) {
            do res += "FF"
            while (++ncount < len)
        }
        //nProNum++
        //return(rval)

        return res
    }

    private fun Snd_D_Chk(cmdstr: String, adrstr: String, datstr: String, nrblock: Int) {
        dataToWrite.add(SendData(cmdstr, adrstr, datstr, nrblock))
    }

    private fun AddToImg(adrstr: String, datstr: String) {
        imageWrite[adrstr] = datstr
        // Log.i(Const.Logging.PACK, "$adrstr($datstr)\n")
    }

    override fun createMessageObject(data: SendData): DataTXMessage {
        val mSendMesageData = DataTXMessage()
        var index = 0
        if (data.command.isNotEmpty()) {
            mSendMesageData.buffer[index++] = Const.Data.SOH

            for (char in data.command) {
                mSendMesageData.buffer[index++] = char.toByte()
                mSendMesageData.bcc = mSendMesageData.bcc xor char.toByte()
            }
            mSendMesageData.bcc = mSendMesageData.bcc xor Const.Data.STX
        }
        mSendMesageData.buffer[index++] = Const.Data.STX

        for (char in data.address) {
            mSendMesageData.buffer[index++] = char.toByte()
            mSendMesageData.bcc = mSendMesageData.bcc xor char.toByte()
        }

        mSendMesageData.buffer[index++] = '('.toByte()
        mSendMesageData.bcc = mSendMesageData.bcc xor '('.toByte()

        for (char in data.data) {
            mSendMesageData.buffer[index++] = char.toByte()
            mSendMesageData.bcc = mSendMesageData.bcc xor char.toByte()
        }
        mSendMesageData.buffer[index++] = ')'.toByte()
        mSendMesageData.bcc = mSendMesageData.bcc xor ')'.toByte()

        if (data.nrBlock != 0) {
            mSendMesageData.buffer[index++] = Const.Data.EOT
            mSendMesageData.bcc = mSendMesageData.bcc xor Const.Data.EOT
        } else {
            mSendMesageData.buffer[index++] = Const.Data.ETX
            mSendMesageData.bcc = mSendMesageData.bcc xor Const.Data.ETX
        }

        mSendMesageData.buffer[index++] = mSendMesageData.bcc
        mSendMesageData.count = index
        return mSendMesageData
    }

    override fun createMessageObject(string: String): DataTXMessage {
        val mSendMesageData = DataTXMessage()
        var index = 0
        if (string.isNotEmpty()) {
            mSendMesageData.buffer[index++] = Const.Data.SOH
            for (char in string) {
                mSendMesageData.buffer[index++] = char.toByte()
                mSendMesageData.bcc = mSendMesageData.bcc xor char.toByte()
            }
        }

        mSendMesageData.buffer[index++] = Const.Data.ETX
        mSendMesageData.bcc = mSendMesageData.bcc xor Const.Data.ETX
        mSendMesageData.buffer[index++] = mSendMesageData.bcc
        mSendMesageData.count = index
        return mSendMesageData
    }

    override fun createMTKCommandMessageObject(string: String): DataTXMessage {
        val mSendMesageData = DataTXMessage()
        if (data.mSoftwareVersion >= 90)
            return createMessageObject(string)
        return mSendMesageData
    }

    override fun isReadImageValid(dataRXMessage: DataRXMessage) {
        imageRead.clear()
        val allImages = mutableMapOf<String, String>()
        val data = dataRXMessage.buffer.take(dataRXMessage.count)
        val dataString = hexToAscii(byteArrayToHexString(data.toByteArray()))
        val lines = dataString.trimAndSplit()
        for (line in lines) {
            val splitLine = line.trim().split("(", ")")
            if (splitLine.size >= 2) {
                val address = removeNonAlphanumeric(splitLine[0])
                val addressData = splitLine[1]
                if (address in Const.Data.adressesC) {
                    imageRead[address] = addressData
                }
                allImages[address] = addressData
            }
        }
        var NrTpar = 11// TODO(remove hardcoding) data.mCfg.cNpar
        val brProg = 9 // TODO(remove hardcoding) data.mCfg.cNprog
        val brRel = 3 + 1 // TODO(remove hardcoding)data.mCfg.cNrel + 1

        var rel = 1

        do {
            var nProNum = 0
            do {
                val address = String.format("%01X%01X80", rel, nProNum)
                val addressData = allImages[address]
                addressData?.let { imageRead[address] = addressData }
                nProNum++
            } while (nProNum < brProg)
            rel++
        } while (rel < brRel)

        for (address in Const.Data.adressesC) {
            if (imageRead[address] == null || imageWrite[address] == null)
                throw VerificationException()
        }

        if (imageRead.size != imageWrite.size)
            throw VerificationException()


        val wrong = mutableListOf<String>()
        for (address in imageRead.keys) {
            val readValue = imageRead[address]
            val writeValue = imageWrite[address]

            when (address) {
                "C080" -> {
                    if (readValue != writeValue!!.substring(0, 36))
                        wrong.add(address)
                }
                "8280" -> {
                    if (readValue!!.substring(0, 20) != writeValue)
                        wrong.add(address)
                }
                else -> {
                    if (readValue != writeValue)
                        wrong.add(address)
                }
            }
        }
        if (wrong.isNotEmpty())
            throw VerificationException()
    }

}
