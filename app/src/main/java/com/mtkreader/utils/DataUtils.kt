package com.mtkreader.utils

object DataUtils {

    fun extractHeaderAndBody(data: String): Pair<ByteArray, ByteArray> {
        val splitIndex = data.indexOf("\n")
        val headerString = data.substring(0, splitIndex)
        val dataString = data.substring(splitIndex + 1)

        return Pair(headerString.toByteArray(), dataString.toByteArray())
    }
}