package com.mtkreader.services

import android.content.Context
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.compare
import com.mtkreader.contracts.ParamsWriteContract
import com.mtkreader.data.reading.*
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
    private val mCfg = CfgParHwsw()
    private var mFileComment = ""
    private val mOprij = Oprij()
    private val mOp50rij = Oprij50()
    private var mPBuff = ByteArray(256)

    private val addressMap = mutableMapOf<String, ByteArray>()

    companion object {
        private const val FILE_TOKEN = "//Programiranje"
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

        var data = addressMap["8080"]
        if (data != null)
            mPBuff = data

        if (mSoftwareVersion >= 80) {
            getKlDatVer9file()
        }

        globalIndex = 0
        data = addressMap["8180"]
        if (data != null)
            mPBuff = data
        getVerAdrParVer6()

        globalIndex = 0
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
                                value += ((c - '0').toInt() * 10.0.pow(i)).toInt()
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
            } else throw Error("Wrong software version")

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