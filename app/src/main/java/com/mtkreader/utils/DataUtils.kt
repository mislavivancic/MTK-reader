package com.mtkreader.utils

import com.mtkreader.commons.Const
import com.mtkreader.data.reading.LadderNets
import com.mtkreader.data.reading.StrParFil
import com.mtkreader.data.reading.StrParFilVer9
import java.util.regex.Matcher
import java.util.regex.Pattern

object DataUtils {
    private val HEXADECIMAL_PATTERN: Pattern = Pattern.compile("\\p{XDigit}+")


    fun extractHeaderAndBody(data: String): Pair<ByteArray, ByteArray> {
        val splitIndex = data.indexOf("\n")
        val headerString = data.substring(0, splitIndex)
        val dataString = data.substring(splitIndex + 1)

        return Pair(headerString.toByteArray(), dataString.toByteArray())
    }

    fun getTbParFilteraVer9(): List<StrParFilVer9> {
        return listOf(
            StrParFilVer9(21, 21, 0x0A20, 0, 1800, 1520, 0x0492, 0x0101, 0, 175.00),
            StrParFilVer9(22, 22, 0x0730, 0, 2650, 2450, 0x045D, 0x0101, 1, 183.3333333),
            StrParFilVer9(23, 23, 0x058A, 0, 2150, 2000, 0x042D, 0x0101, 2, 194.00),
            StrParFilVer9(25, 25, 0x0920, 0, 2430, 2375, 0x03D7, 0x0101, 3, 208.3333333),
            StrParFilVer9(26, 26, 0x09C8, 0, 3800, 3600, 0x03B1, 0x0101, 4, 216.6666667),
            StrParFilVer9(27, 27, 0x0A20, 0, 1800, 1520, 0x038E, 0x0102, 5, 225.0),
            StrParFilVer9(28, 28, 0x0A20, 0, 1800, 1520, 0x036E, 0x0102, 6, 233.3333333),
            StrParFilVer9(32, 16, 0x0A20, 0, 1800, 1520, 0x0300, 0x0102, 7, 266.6666667),
            StrParFilVer9(34, 34, 0x0DE0, 0, 5000, 4860, 0x02D3, 0x0101, 8, 283.3333333),
            StrParFilVer9(38, 38, 0x0C20, 0, 2520, 2400, 0x0287, 0x0002, 9, 316.66666670),
            StrParFilVer9(20, 20, 0x0A20, 0, 1800, 1520, 0x0266, 0x0102, 10, 333.33333331),
            StrParFilVer9(22, 22, 0x0A20, 0, 1800, 1520, 0x022F, 0x0102, 11, 366.66666672),
            StrParFilVer9(23, 23, 0x0A20, 0, 1800, 1520, 0x0216, 0x0102, 12, 383.33333333),
            StrParFilVer9(25, 25, 0x0A20, 0, 2880, 2700, 0x01EC, 0x0100, 13, 416.66666674),
            StrParFilVer9(25, 25, 0x0A20, 0, 2790, 2700, 0x01EB, 0x0100, 14, 420.00),
            StrParFilVer9(26, 26, 0x0A20, 0, 1800, 1520, 0x01D9, 0x0100, 15, 433.33333336)
        )
    }

    fun getTbparfiltera98mhz(): List<StrParFil> {
        return listOf(
            StrParFil(21, 21, 0, 0, 0x120, 0x80, 0xDB7, 2, 2, 175.00),
            StrParFil(22, 22, 0, 0, 0x120, 0x80, 0x0D17, 2, 1, 183.3333333),
            StrParFil(23, 23, 0, 0, 0x120, 0x80, 0x0C86, 2, 2, 191.6666667),
            StrParFil(25, 25, 0, 0, 0x116, 0xF2, 0x0B85, 2, 3, 208.3333333),
            StrParFil(26, 26, 0, 0, 0x116, 0xF2, 0x0B13, 2, 4, 216.6666667),
            StrParFil(27, 27, 0, 0, 0x120, 0x80, 0x0AAB, 2, 5, 225.0),
            StrParFil(28, 28, 0, 0, 0x120, 0x80, 0x0A49, 2, 6, 233.3333333),
            StrParFil(32, 16, 0, 0, 0x120, 0x80, 0x0900, 2, 7, 266.6666667),
            StrParFil(34, 17, 0, 0, 0x108, 0xB0, 0x0878, 2, 8, 283.3333333),
            StrParFil(38, 19, 0, 0, 0x108, 0xB0, 0x0794, 2, 9, 316.6666667),
            StrParFil(20, 20, 0, 0, 0x120, 0x80, 0x0733, 2, 10, 333.3333333),
            StrParFil(22, 22, 0, 0, 0x120, 0x80, 0x068C, 2, 11, 366.6666667),
            StrParFil(23, 23, 0, 0, 0x120, 0x80, 0x0643, 2, 12, 383.3333333),
            StrParFil(25, 25, 0, 0, 0x120, 0x80, 0x05C2, 3, 13, 416.6666667),
            StrParFil(25, 25, 0, 0, 0x136, 0xF6, 0x05C2, 3, 14, 420.00),
            StrParFil(26, 26, 0, 0, 0x120, 0x80, 0x058A, 3, 15, 433.3333333)
        )
    }

    fun tbParFiltera(): List<StrParFil> {
        return listOf(
            StrParFil(21, 21, 0, 0, 0x120, 0x80, 0xDB7, 2, 2, 175.00),
            StrParFil(22, 22, 0, 0, 0x120, 0x80, 0x0D17, 2, 1, 183.3333333),
            StrParFil(23, 23, 0, 0, 0x120, 0x80, 0x0C86, 2, 2, 191.6666667),
            StrParFil(25, 25, 0, 0, 0x116, 0xF2, 0x0B85, 2, 3, 208.3333333),
            StrParFil(26, 26, 0, 0, 0x116, 0xF2, 0x0B13, 2, 4, 216.6666667),
            StrParFil(27, 27, 0, 0, 0x120, 0x80, 0x0AAB, 2, 5, 225.0),
            StrParFil(28, 28, 0, 0, 0x120, 0x80, 0x0A49, 2, 6, 233.3333333),
            StrParFil(32, 16, 0, 0, 0x120, 0x80, 0x0900, 2, 7, 266.6666667),
            StrParFil(34, 17, 0, 0, 0x108, 0xB0, 0x0878, 2, 8, 283.3333333),
            StrParFil(38, 19, 0, 0, 0x108, 0xB0, 0x0794, 2, 9, 316.6666667),
            StrParFil(20, 20, 0, 0, 0x120, 0x80, 0x0733, 2, 10, 333.3333333),
            StrParFil(22, 22, 0, 0, 0x120, 0x80, 0x068C, 2, 11, 366.6666667),
            StrParFil(23, 23, 0, 0, 0x120, 0x80, 0x0643, 2, 12, 383.3333333),
            StrParFil(25, 25, 0, 0, 0x120, 0x80, 0x05C2, 3, 13, 416.6666667),
            StrParFil(25, 25, 0, 0, 0x136, 0xF6, 0x05C2, 3, 14, 420.00),
            StrParFil(26, 26, 0, 0, 0x120, 0x80, 0x058A, 3, 15, 433.3333333)
        )
    }

    fun getParRasTlgVer9(): Array<Array<Int>> {
        return arrayOf(
            arrayOf(
                0x0000,
                0X002B,
                0X0050,
                0X0090,
                0X004A,
                0X005E,
                0X007B,
                0X000C,
                0X0012,
                0X001E,
                0X003E,
                0X0053,
                0X0073,
                0X0074,
                0X0090,
                0X0043,
                0x0032,
                0x0000
            ),
            arrayOf(
                0x0001,
                0X0050,
                0X00A0,
                0X0100,
                0X0084,
                0X009C,
                0X00C8,
                0X0019,
                0X0027,
                0X0040,
                0X005A,
                0X0066,
                0X0080,
                0X0080,
                0X007E,
                0X0055,
                0x0032,
                0x0000
            ),
            arrayOf(
                0X0002,
                0X0050,
                0X00B4,
                0X012C,
                0X013B,
                0X01B3,
                0X028A,
                0X003C,
                0X008C,
                0X00C8,
                0X0136,
                0X0186,
                0X01F4,
                0X01F4,
                0X012C,
                0X014A,
                0X0028,
                0x0000
            ),
            arrayOf(
                0x0003,
                0X002B,
                0X0050,
                0X0090,
                0X004A,
                0X005E,
                0X007B,
                0X000C,
                0X0012,
                0X001E,
                0X003E,
                0X0052,
                0X0073,
                0X0073,
                0X0090,
                0X0043,
                0x0032,
                0x0000
            ),
            arrayOf(
                0x0004,
                0x00CF,
                0x019E,
                0x0258,
                0x019C,
                0x028C,
                0x033F,
                0x002C,
                0x009C,
                0x00C8,
                0x00E3,
                0x00EF,
                0x010A,
                0x010A,
                0x0258,
                0x00E6,
                0x002C,
                0x0000
            ),
            arrayOf(
                0x0005,
                0x0096,
                0x012C,
                0x0186,
                0x00F4,
                0x011C,
                0x016B,
                0x001A,
                0x0028,
                0x0042,
                0x005D,
                0x0069,
                0x0084,
                0x0084,
                0x0186,
                0x0060,
                0x002C,
                0x0000
            ),
            arrayOf(
                0X0006,
                0X00BE,
                0X017C,
                0X0258,
                0X019C,
                0X028C,
                0X033F,
                0X002C,
                0X009C,
                0X00C8,
                0X00E3,
                0X00EF,
                0X010A,
                0X010A,
                0X0258,
                0X00E6,
                0X0032,
                0X0000
            ),
            arrayOf(
                0x0007,
                0x007D,
                0x00FA,
                0x0186,
                0x00F4,
                0x011C,
                0x016B,
                0x001A,
                0x0028,
                0x0042,
                0x005D,
                0x0069,
                0x0084,
                0x0084,
                0x0186,
                0x0060,
                0x0032,
                0x0000
            ),
            arrayOf(
                0x0008,
                0X002B,
                0X0050,
                0X0090,
                0X004A,
                0X005E,
                0X007B,
                0X000C,
                0X0012,
                0X001E,
                0X003E,
                0X0052,
                0X0073,
                0X0073,
                0X0090,
                0X0043,
                0x0032,
                0x0000
            )
        )
    }


    fun getIVtmask(index: Int): Int {
        return intArrayOf(
            0x8000,
            0x4000,
            0x2000,
            0x1000,
            0x0800,
            0x0400,
            0x0200,
            0x0100,
            0x0080,
            0x0040,
            0x0020,
            0x0010,
            0x0008,
            0x0004,
            0x0002,
            0x0001
        )[index]
    }

    fun getBVtmask(index: Int): Int {
        return intArrayOf(0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01)[index]

    }

    fun getPLCfg(index: Int): LadderNets {
        return arrayOf(
            LadderNets(
                1, byteArrayOf(0, 0, 0, 0),
                byteArrayOf(0, 0, 0, 0),
                booleanArrayOf(true, true, true, true),
                booleanArrayOf(true, true, true, true),
                booleanArrayOf(false, false, false, false)
            ),
            LadderNets(
                2,
                byteArrayOf(15, 0, 0, 0),
                byteArrayOf(15, 0, 0, 0),
                booleanArrayOf(true, false, false, false),
                booleanArrayOf(true, false, false, false),
                booleanArrayOf(false, false, false, false)
            ),
            LadderNets(
                3,
                byteArrayOf(0, 0, 15, 0),
                byteArrayOf(0, 0, 15, 0),
                booleanArrayOf(false, false, true, false),
                booleanArrayOf(false, false, true, false),
                booleanArrayOf(false, false, false, false)
            ),
            LadderNets(
                4,
                byteArrayOf(15, 15, 0, 0),
                byteArrayOf(15, 15, 0, 0),
                booleanArrayOf(false, false, true, false),
                booleanArrayOf(true, false, true, false),
                booleanArrayOf(false, false, false, false)
            ),
            LadderNets(
                5,
                byteArrayOf(0, 0, 15, 15),
                byteArrayOf(0, 0, 15, 15),
                booleanArrayOf(true, false, false, false),
                booleanArrayOf(true, false, true, false),
                booleanArrayOf(false, false, false, false)
            ),
            LadderNets(
                6,
                byteArrayOf(15, 0, 15, 0),
                byteArrayOf(15, 0, 15, 0),
                booleanArrayOf(true, false, true, false),
                booleanArrayOf(true, false, true, false),
                booleanArrayOf(false, false, false, false)
            ),
            LadderNets(
                7,
                byteArrayOf(15, 15, 15, 0),
                byteArrayOf(15, 15, 15, 0),
                booleanArrayOf(false, false, true, false),
                booleanArrayOf(true, false, true, false),
                booleanArrayOf(false, false, false, false)
            ),
            LadderNets(
                8,
                byteArrayOf(15, 0, 15, 15),
                byteArrayOf(15, 0, 15, 15),
                booleanArrayOf(true, false, false, false),
                booleanArrayOf(true, false, true, false),
                booleanArrayOf(false, false, false, false)
            ),
            LadderNets(
                9,
                byteArrayOf(15, 0, 15, 15),
                byteArrayOf(15, 0, 15, 15),
                booleanArrayOf(true, false, true, true),
                booleanArrayOf(false, false, false, false),
                booleanArrayOf(false, true, false, false)
            ),
            LadderNets(
                10,
                byteArrayOf(15, 15, 15, 0),
                byteArrayOf(15, 15, 15, 0),
                booleanArrayOf(true, true, true, false),
                booleanArrayOf(false, false, false, false),
                booleanArrayOf(false, false, false, true)
            ),
            LadderNets(
                13,
                byteArrayOf(15, 15, 15, 15),
                byteArrayOf(15, 15, 15, 15),
                booleanArrayOf(true, true, true, true),
                booleanArrayOf(false, false, false, false),
                booleanArrayOf(false, false, false, false)
            ),
            LadderNets(
                14,
                byteArrayOf(15, 15, 15, 15),
                byteArrayOf(15, 15, 15, 15),
                booleanArrayOf(false, false, false, false),
                booleanArrayOf(false, false, false, false),
                booleanArrayOf(false, false, false, false)
            )
        )[index]
    }


    fun HtoB(ch: Char): Char {
        if (ch in '0'..'9') {
            return (ch - '0').toChar()
        }
        if (ch in 'A'..'F') {
            return (ch - 'A' + 0xA).toChar()
        }
        return ch
    }

    fun getHardwareVersion(header: ByteArray): Int {
        val headString = header.toString(Charsets.UTF_8)
        for ((cnt, version) in Const.Data.CTipPrij.withIndex()) {
            if (headString.contains(version, true)) {
                return cnt
            }
        }
        return 0
    }

    fun isHexadecimal(input: String): Boolean {
        val matcher: Matcher = HEXADECIMAL_PATTERN.matcher(input)
        return matcher.matches()
    }

    fun removeNonAlphanumeric(input: String): String {
        val re = Regex("[^A-Za-z0-9 ]")
        return re.replace(input, "")
    }

    fun byteArrayToHexString(bytes: ByteArray): String {
        var data = ""
        for (byte in bytes) {
            data += String.format("%02X", byte)
        }
        return data
    }

    fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                    + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    fun hexToAscii(hexStr: String): String {
        val output = StringBuilder("")
        var i = 0
        while (i < hexStr.length) {
            val str = hexStr.substring(i, i + 2)
            output.append(str.toInt(16).toChar())
            i += 2
        }
        return output.toString()
    }
}