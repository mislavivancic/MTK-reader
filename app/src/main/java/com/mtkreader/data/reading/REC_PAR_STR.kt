package com.mtkreader.data.reading

class REC_PAR_STR {
    companion object {
        fun StringToByteArrTrimed(s: String, len: Int): ByteArray {
            var b = s.toByteArray(Charsets.US_ASCII)
            var L = ByteArray(PARIDFILE_SIZE)
            var i = 0
            while (i < len) {
                if (i < s.length) L[i] = b[i]
                else L[i] = 0
                i++
            }

            return L.take(len).toByteArray()
        }

        const val DataTime_SIZE = 6
        const val PARID_SIZE = 8
        const val PARIDFILE_SIZE = 18
    }

    var DataTime = ByteArray(DataTime_SIZE)

    var CreateSite = ByteArray(PARID_SIZE)
    var IDCreate = ByteArray(PARID_SIZE)
    var ReParaSite = ByteArray(PARID_SIZE)
    var IDRePara = ByteArray(PARID_SIZE)
    var IDFile = ByteArray(PARIDFILE_SIZE)

    val CreateSiteS: String get() = TrimByteArrToString(CreateSite)
    val IDCreateS: String get() = TrimByteArrToString(IDCreate)
    val ReParaSiteS: String get() = TrimByteArrToString(ReParaSite)
    val IDReParaS: String get() = TrimByteArrToString(IDRePara)
    val IDFileS: String get() = TrimByteArrToString(IDFile)


    override fun toString(): String {


        val str2 = String.format(
            "IDCreate %s CreateSite %s IDRePara %s ReParaSite %s IDFile %s .mtk",
            TrimByteArrToString(CreateSite),
            TrimByteArrToString(IDCreate),
            TrimByteArrToString(ReParaSite),
            TrimByteArrToString(IDRePara),
            TrimByteArrToString(IDFile)

        )
        return DataTimeS + str2
    }

    val DataTimeS: String
        get() =
            String.format(
                "%02X-%02X-%02X %02X:%02X",
                DataTime[3],
                DataTime[4],
                DataTime[5],
                DataTime[2],
                DataTime[1]

            )


    fun TrimByteArrToString(s: ByteArray): String {
        var trim = s.filter { it != 0.toByte() }.toByteArray()
        return trim.toString(Charsets.US_ASCII)
    }


}