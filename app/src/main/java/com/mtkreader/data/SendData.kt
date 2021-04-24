package com.mtkreader.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SendData(val command: String, val address: String, val data: String, val nrBlock: Int) : Parcelable