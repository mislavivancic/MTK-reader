package com.mtkreader.services

import com.mtkreader.contracts.ParamsWriteContract
import com.mtkreader.data.DataStructures
import com.mtkreader.data.reading.Uni4byt
import com.mtkreader.utils.DataUtils
import io.reactivex.Single
import kotlin.experimental.and
import kotlin.experimental.inv

class WriteDataService : ParamsWriteContract.WriteDataService {

    private lateinit var data: DataStructures

    override fun generateStrings(data: DataStructures): Single<String> {
        this.data = data
        return Single.fromCallable { setData() }
    }

    private fun setData(): String {
        setFrRaParVer9(false)

        return ""
    }

    private fun setFrRaParVer9(bbdefault: Boolean) {
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

        var pchData: String = ""
        val ttt = Uni4byt(data.mParFilteraCF.BROJ)
        pchData = String.format(
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

        println()
    }


}