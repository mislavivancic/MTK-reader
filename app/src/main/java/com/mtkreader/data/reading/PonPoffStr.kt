package com.mtkreader.data.reading

data class PonPoffStr(
    val status: Byte,
    val OnPonExe: Byte,
    val lperIgno: Byte,
    val TminSwdly: Int,
    val TrndSwdly: Int,
    val Tlng: Int,
    val lOnPonExe: Byte,
    val OnPoffExe: Byte,
    val TBlockPrePro: Int
)