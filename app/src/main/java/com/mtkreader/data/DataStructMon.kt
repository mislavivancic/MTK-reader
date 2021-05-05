package com.mtkreader.data
import android.content.Context
import com.mtkreader.R
import com.mtkreader.data.reading.WTONOFF
import com.mtkreader.hasFlag
import org.koin.core.KoinComponent
import org.koin.core.inject

class DataStructMon: KoinComponent {
    val context: Context by inject()
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
var m_AktRel=0


    var dispR_TransmitFail = Array(4) { "" }
    var dispR_LoopEn = Array(4) { "" }
    var dispR_ProgEn = Array(4) { "" }
    var dispR_Learn = Array(4) { "" }
    var dispR_Wiper = Array(4) { "" }
    var dispR_BrPrek = Array(4) { "" }

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
    var disp_eventlogCSV = ""
    private fun getString(resId: Int): String {
        return context.resources.getString(resId)
    }
    fun dispStatus(): String {
        var status = ""
        val sep = ":"
        val sep2 = "\t"
        val crlf = "\r\n"
        status += getString(R.string.dmonitor_paramfile) + sep + disp_paramfile + ".mtk"+crlf
        status += getString(R.string.dmonitor_devicetime) + sep + disp_date + " " + disp_time + crlf
        status += getString(R.string.dmonitor_RTC) + sep + disp_RTC + " SYNC:" + disp_RTCsync + crlf
        status += getString(R.string.dmonitor_timeinOp) + sep + disp_timeInOp + crlf
        status += getString(R.string.dmonitor_outages) + sep + disp_outage + crlf
        status += getString(R.string.dmonitor_UTF) + sep + disp_UTF + crlf
        status += getString(R.string.dmonitor_lastTlg) + sep + disp_lastTlg + crlf

        var dmonitor_rel = crlf + sep2

        var dmonitor_wiper = crlf + getString(R.string.dmonitor_wiper) + sep2
        var dmonitor_learn = crlf + getString(R.string.dmonitor_learn) + sep2
        var dmonitor_programs = crlf + getString(R.string.dmonitor_programs) + sep2
        var dmonitor_loop = crlf + getString(R.string.dmonitor_loop) + sep2
        var dmonitor_transmiterfail = crlf + getString(R.string.dmonitor_transmiterfail) + sep2
        var dmonitor_switchingcnt = crlf + getString(R.string.dmonitor_switchingcnt) + sep2
        val empty = ""
        for (i in 0..3) {
            val flg = m_AktRel.hasFlag(0x80 shr i)
            dmonitor_rel += if (flg) String.format(getString(R.string.relay_num), i+1)+sep2 else empty+sep2
            dmonitor_wiper += if (flg) dispR_Wiper[i]+sep2 else empty+sep2
            dmonitor_learn += if (flg) dispR_Learn[i]+sep2 else empty+sep2
            dmonitor_programs += if (flg) dispR_ProgEn[i]+sep2 else empty+sep2
            dmonitor_loop += if (flg) dispR_LoopEn[i]+sep2 else empty+sep2
            dmonitor_transmiterfail += if (flg) dispR_TransmitFail[i]+sep2 else empty+sep2
            dmonitor_switchingcnt += if (flg) dispR_BrPrek[i]+sep2 else empty+sep2
        }
        status+=dmonitor_rel
        status+=dmonitor_wiper
        status+=dmonitor_learn
        status+=dmonitor_programs
        status+=dmonitor_loop
        status+=dmonitor_transmiterfail
        status+=dmonitor_switchingcnt
        return status

    }
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