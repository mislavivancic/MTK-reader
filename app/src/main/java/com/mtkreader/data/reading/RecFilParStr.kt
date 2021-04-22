package com.mtkreader.data.reading

class RecFilParStr {

    companion object {
        const val PARID_SIZE = 8
        const val PARIDFILE_SIZE = 16
    }

    var CreateSite = ByteArray(PARID_SIZE)
    var IDCreate = ByteArray(PARID_SIZE)
    var IDFile = ByteArray(PARIDFILE_SIZE)

    val CreateSiteS:String get()=TrimByteArrToString(CreateSite)
    val IDCreateS:String get()=TrimByteArrToString(IDCreate)
    val IDFileS:String get()=TrimByteArrToString(IDFile)


    public fun TrimByteArrToString(s: ByteArray): String {
        var trim = s.filter { it != 0.toByte() }.toByteArray()
        return trim.toString(Charsets.US_ASCII)
    }

}