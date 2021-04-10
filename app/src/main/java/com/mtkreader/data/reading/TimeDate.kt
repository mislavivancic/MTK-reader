package com.mtkreader.data.reading

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TimeDate(
    var sek: Byte = 0,
    var min: Byte = 0,
    var sat: Byte = 0,
    var dan: Byte = 0,
    var dat: Byte = 0,
    var mje: Byte = 0,
    var god: Byte = 0
) : Parcelable {

    fun getArray(): ByteArray {
        return byteArrayOf(sek, min, sat, dan, dat, mje, god)
    }
}