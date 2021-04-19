package com.mtkreader.services

import com.mtkreader.contracts.ParamsWriteContract
import com.mtkreader.data.DataStructures
import com.mtkreader.data.reading.Telegram
import com.mtkreader.data.reading.Tlg
import com.mtkreader.data.reading.Uni4byt
import com.mtkreader.getBytes
import com.mtkreader.utils.DataUtils
import io.reactivex.Single
import kotlin.experimental.inv

class WriteDataService : ParamsWriteContract.WriteDataService {

    private lateinit var data: DataStructures

    override fun generateStrings(data: DataStructures): Single<String> {
        this.data = data
        return Single.fromCallable { setData() }
    }

    private fun setData(): String {
        setFrRaParVer9(false)
        setTlgData(0, 2)
        setTlgData(1, 2)
        setTlgData(2, 2)
        setTlgData(3, 2)
        setTlgData(4, 3)
        setTlgData(5, 2)
        setTlgData(6, 3)


        return ""
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

        for (byte in pbuf) {
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


}