package com.mtkreader.data

class DataStructMon {

    companion object {
        public const val LOG_TLG_MAX = 16
        public const val LOG_EVENT_MAX = 100
    }



   //STR_TLG_LOG_H  m_TlgLogH
   //STR_TLG_LOG_O   m_TlgLog[LOG_TLG_MAX]
   //STR_EVENT_LOG_H  m_EvLogH
   //STR_EVENT_LOG    m_EvLog[LOG_EVENT_MAX]
var m_EvLog=Array(LOG_EVENT_MAX){STR_EVENT_LOG()}

var m_EvLogH=STR_EVENT_LOG_H()
    var dispR1_BrPrek=""
    var dispR2_BrPrek=""
    var dispR3_BrPrek=""
    var dispR4_BrPrek=""

    var dispR_TransmitFail=Array(4){""}
    var dispR_LoopEn=Array(4){""}
    var dispR_ProgEn=Array(4){""}
    var dispR_Learn=Array(4){""}
    var dispR_Wiper=Array(4){""}


    var disp_paramfile=""
    var disp_time=""
    var disp_date=""
    var disp_RTC=""
    var disp_RTCsync=""
    var disp_timeInOp=""
    var disp_outage=""
    var disp_UTF=""
    var disp_lastTlg=""
    var disp_serNum=""

    var disp_eventlog=""
    var disp_learncycle=""


}


//typedef struct {
//
//    STR_LOG_TIME Time;
//    byte Obj;
//    UN_STR_EVENT  Event;
//} STR_EVENT_LOG;
class STR_EVENT_LOG_H {
    var indx: Byte = 0
    var start: Byte = 0
    var MaxEvent: Byte = 0
    var Event: Byte = 0

}

class STR_EVENT_LOG{
  var   Time=STR_LOG_TIME()
    var Obj=0
    var Event=UN_STR_EVENT()
}

class STR_LOG_TIME{
    var sec=0
    var min=0
    var hour=0
    var day=0
    var dat=0
    var month=0
    var year=0

}
class STR_OEVENT{
    var bH=0
    var bL=0
}

class STR_TLG_LOG{
    var Imp=ByteArray(8)

}
class UN_STR_EVENT {
    var Imp=ByteArray(8)
    var bH=0
    var bL=0
}