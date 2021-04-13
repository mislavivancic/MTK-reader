package com.mtkreader.services

import android.content.Context
import com.mtkreader.R
import com.mtkreader.contracts.ParamsWriteContract
import com.mtkreader.data.reading.CfgParHwsw
import com.mtkreader.data.reading.StrParFil
import com.mtkreader.data.reading.StrParFilVer9
import com.mtkreader.utils.DataUtils
import com.mtkreader.utils.DataUtils.isHexadecimal
import com.mtkreader.utils.DataUtils.removeNonAlphanumeric
import io.reactivex.Completable
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.Long

class ParamsWriteService : ParamsWriteContract.Service, KoinComponent {

    private val context: Context by inject()

    private var mTip = 0
    private var mHardwareVersion = 0
    private var mSoftwareVersion = 0
    private var mParFiltera = StrParFil()
    private val mParFilteraCF = StrParFilVer9()
    private var mBrojRast = 0
    private var mUtfPosto = 0.0
    private val mCfg = CfgParHwsw()
    private var mFileComment = ""

    private val addressMap = mutableMapOf<String, String>()

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
    }

    private fun fillAddressMap(lines: List<String>) {
        for (line in lines) {
            println(line)
            val addressData = line.split("(")
            val address = addressData.getOrElse(0) { "" }
            val data = addressData.getOrElse(1) { "" }
            addressMap[address] = data
        }
        println()
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