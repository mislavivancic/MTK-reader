package com.mtkreader.data.writing

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DataTXTMessage(
    var count: Int = 0,
    val status: Byte = 0,
    val type: Byte = 0,
    var bcc: Byte = 0,
    val buffer: ByteArray = ByteArray(2048 * 4)
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataTXTMessage

        if (count != other.count) return false
        if (status != other.status) return false
        if (type != other.type) return false
        if (bcc != other.bcc) return false
        if (!buffer.contentEquals(other.buffer)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = count
        result = 31 * result + status
        result = 31 * result + type
        result = 31 * result + bcc
        result = 31 * result + buffer.contentHashCode()
        return result
    }
}