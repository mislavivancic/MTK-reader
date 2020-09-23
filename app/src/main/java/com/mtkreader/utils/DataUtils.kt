package com.mtkreader.utils

import com.mtkreader.data.reading.StrParFil
import com.mtkreader.data.reading.StrParFilVer9

object DataUtils {

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
            StrParFilVer9(26, 26, 0x0A40, 0, 3800, 3600, 0x03B1, 0x0101, 4, 216.6666667),
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

    fun tbparfiltera98mhz(): List<StrParFil> {
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
}