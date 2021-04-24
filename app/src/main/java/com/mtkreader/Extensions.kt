package com.mtkreader

import com.mtkreader.data.reading.TelegCMD
import com.mtkreader.data.reading.Telegram
import com.mtkreader.data.reading.Telegrel

/**
 * Compares this value with the specified value for order.
 * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
 * or a positive number if it's greater than other.
 */
fun Byte.compare(other: Int): Int {
    var unsignedValue = this.toInt()
    if (this < 0)
        unsignedValue = -this.toInt()
    return unsignedValue.compareTo(other)
}

fun Byte.toPositiveInt(): Int {
    return toInt() and 0xFF
}

fun Telegrel.getBytes(): ByteArray {
    val bytes = mutableListOf<Byte>()
    bytes.addAll(Uk.getBytes().toList())
    bytes.addAll(Isk.getBytes().toList())
    return bytes.toByteArray()
}

fun Telegram.getBytes(): ByteArray {
    val bytes = mutableListOf<Byte>()
    bytes.addAll(Cmd.getBytes().toList())
    return bytes.toByteArray()
}

fun TelegCMD.getBytes(): ByteArray {
    val bytes = mutableListOf<Byte>()
    bytes.addAll(AktiImp.toList())
    bytes.add(BrAkImp)
    bytes.addAll(NeutImp.toList())
    bytes.add(Fn)
    return bytes.toByteArray()
}

fun Byte.getAsArray(): ByteArray {
    return byteArrayOf(this)
}

fun String.trimAndSplit(): List<String> {
    return trim().lines().filter { it.isNotBlank() }
}