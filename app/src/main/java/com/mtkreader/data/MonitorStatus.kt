package com.mtkreader.data

data class MonitorStatus(
    val parameterFile: String,
    val deviceTime: String,
    val RTC: String,
    val timeInOperation: String,
    val outageCount: String,
    val utfV: String,
    val lastTelegram: String,

    val relayStatuses: MutableList<RelayStatus> = mutableListOf()
)

data class RelayStatus(
    val relayNum: String,
    val wiper: String,
    val learn: String,
    val programs: String,
    val loop: String,
    val transmitterFail: String,
    val switchingCount: String
)
