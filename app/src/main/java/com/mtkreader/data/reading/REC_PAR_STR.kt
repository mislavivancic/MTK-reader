package com.mtkreader.data.reading
//#define PARIDFILE_SIZE 18
//#define PARID_SIZE 8

class REC_PAR_STR {
    companion object {
        public const val DataTime_SIZE = 6
        public const val PARID_SIZE = 8
        public const val PARIDFILE_SIZE = 18
    }

    var DataTime = ByteArray(DataTime_SIZE)

    var CreateSite = ByteArray(PARID_SIZE)
    var IDCreate = ByteArray(PARID_SIZE)
    var ReParaSite = ByteArray(PARID_SIZE)
    var IDRePara = ByteArray(PARID_SIZE)
    var IDFile = ByteArray(PARIDFILE_SIZE)

    val CreateSiteS:String get()=TrimByteArrToString(CreateSite)
    val IDCreateS:String get()=TrimByteArrToString(IDCreate)
    val ReParaSiteS:String get()=TrimByteArrToString(ReParaSite)
    val IDReParaS:String get()=TrimByteArrToString(IDRePara)
    val IDFileS:String get()=TrimByteArrToString(IDFile)


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

    val DataTimeS: String get()=
        String.format(
            "%02X-%02X-%02X %02X:%02X",
            DataTime[3],
            DataTime[4],
            DataTime[5],
            DataTime[2],
            DataTime[1]

        )


     public fun TrimByteArrToString(s: ByteArray): String {
        var trim = s.filter { it != 0.toByte() }.toByteArray()
        return trim.toString(Charsets.US_ASCII)
    }
    //byte	DataTime[6];
    //byte  CreateSite[PARID_SIZE];
    //byte  IDCreate[PARID_SIZE];
    //byte  ReParaSite[PARID_SIZE];
    //byte  IDRePara[PARID_SIZE];

    //byte  IDFile[PARIDFILE_SIZE];

    //str.Format(_T("\n\r"+CMsg(IDSI_LASTREPARAMTIME)+" %02X-%02X-%02X %02X:%02X\n\r"+CMsg(IDSI_CREATED_UID)+" %s "+CMsg(IDSI_CREATED_PCUID)+" |%s|\n\r"+CMsg(IDSI_REPARM_UID)+" %s "+CMsg(IDSI_REPARM_PCUID)+" |%s|\n\r"+CMsg(IDSI_PARM_FILE)+"%s.mtk \n\r"), 
    //pLastPa->DataTime[3], pLastPa->DataTime[4], pLastPa->DataTime[5], pLastPa->DataTime[2], pLastPa->DataTime[1], IDCreate, CreateSite, IDRePara, ReParaSite, IDFile );


}