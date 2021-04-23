package com.mtkreader.data

import com.mtkreader.data.reading.WTONOFF

class DataStructMon {

    companion object {
        public const val LOG_TLG_MAX = 16
        public const val LOG_EVENT_MAX = 100
    }


    //STR_TLG_LOG_H  m_TlgLogH
    //STR_TLG_LOG_O   m_TlgLog[LOG_TLG_MAX]
    //STR_EVENT_LOG_H  m_EvLogH
    //STR_EVENT_LOG    m_EvLog[LOG_EVENT_MAX]
    var m_EvLog = Array(LOG_EVENT_MAX) { STR_EVENT_LOG() }
    var m_EvLogH = STR_EVENT_LOG_H()


    var dispR1_BrPrek = ""
    var dispR2_BrPrek = ""
    var dispR3_BrPrek = ""
    var dispR4_BrPrek = ""

    var dispR_TransmitFail = Array(4) { "" }
    var dispR_LoopEn = Array(4) { "" }
    var dispR_ProgEn = Array(4) { "" }
    var dispR_Learn = Array(4) { "" }
    var dispR_Wiper = Array(4) { "" }


    var disp_paramfile = ""
    var disp_time = ""
    var disp_date = ""
    var disp_RTC = ""
    var disp_RTCsync = ""
    var disp_timeInOp = ""
    var disp_outage = ""
    var disp_UTF = ""
    var disp_lastTlg = ""
    var disp_serNum = ""

    var disp_eventlog = ""
    var disp_learncycle = ""


}
class STA7DCSTR {
  var currDay=0 //byte currDay;		//trenutni dan je ujedno je i broj programa
  var StartDay=0 //char StartDay;  // dan kad je startan ciklus
  var BrUpProg=0 //byte BrUpProg;  // broj upisanih programa
  var ctl=0 //byte ctl;        //LEARN_7DAYS_MASK

}
class REL24HCSTR2{
    companion object {
        public const val SIZE_24HC_TPAR2 = 5
    }
    var  Sta7DC=STA7DCSTR()
    var State24h=0		                // Start, break, End;
    var RelWrPos=0		                ////0 == nije primio komandu ; 1== primio komandu ON ; 2== primio komandu OFF  3= obje
    var LastCmd=0		                //0 nepoznata  1 ON 2 OFF
    var NrTpar=0
    var Tsta1Off=0 //short
    var Tpar = Array(SIZE_24HC_TPAR2)  { WTONOFF() }

}
class STR_EVENT_LOG_H {
    var indx: Byte = 0
    var start: Byte = 0
    var MaxEvent: Byte = 0
    var Event: Byte = 0

}

class STR_EVENT_LOG {
    var Time = STR_LOG_TIME()
    var Obj = 0
    var Event = UN_STR_EVENT()
}

class STR_LOG_TIME {
    var sec = 0
    var min = 0
    var hour = 0
    var day = 0
    var dat = 0
    var month = 0
    var year = 0

}

class STR_OEVENT {
    var bH = 0
    var bL = 0
}

class STR_TLG_LOG {
    var Imp = ByteArray(8)

}

class UN_STR_EVENT {
    var Imp = ByteArray(8)
    var bH = 0
    var bL = 0
}