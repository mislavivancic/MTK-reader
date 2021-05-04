package com.mtkreader.services

import android.content.Context
import android.util.Log
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.Const.Data.CLP_REL_X
import com.mtkreader.commons.Const.Data.EMT_REL_X
import com.mtkreader.commons.Const.Data.PON_REL_X
import com.mtkreader.commons.Const.Data.PRO_REL_X
import com.mtkreader.commons.Const.Data.REL_OFF
import com.mtkreader.commons.Const.Data.REL_ON
import com.mtkreader.commons.Const.Data.REL_PROBLOCK
import com.mtkreader.commons.Const.Data.REL_PROUNBLOCK
import com.mtkreader.commons.Const.Data.REL_TA_R
import com.mtkreader.commons.Const.Data.REL_TA_S
import com.mtkreader.commons.Const.Data.REL_WIP_R
import com.mtkreader.commons.Const.Data.REL_WIP_S
import com.mtkreader.commons.Const.Data.SNE_LSINH
import com.mtkreader.commons.Const.Data.SNE_POFF
import com.mtkreader.commons.Const.Data.SNE_PON
import com.mtkreader.commons.Const.Data.SNE_RTC_BL
import com.mtkreader.commons.Const.Data.SNE_RTC_OF
import com.mtkreader.commons.Const.Data.SNE_RTC_OOK
import com.mtkreader.commons.Const.Data.SNE_RTC_ST
import com.mtkreader.commons.Const.Data.SNE_SHD
import com.mtkreader.commons.Const.Data.SNE_SHT
import com.mtkreader.commons.Const.Data.SNE_WPARERR
import com.mtkreader.commons.Const.Data.SNE_WPAROK
import com.mtkreader.commons.Const.Data.SNO_PRIJEM
import com.mtkreader.commons.Const.Data.SNO_REL1
import com.mtkreader.commons.Const.Data.SNO_REL2
import com.mtkreader.commons.Const.Data.SNO_REL3
import com.mtkreader.commons.Const.Data.SNO_REL4
import com.mtkreader.commons.Const.Data.SNO_RTC
import com.mtkreader.commons.Const.Data.TEL_REL_X
import com.mtkreader.contracts.MonitorContract
import com.mtkreader.data.DataStructMon
import com.mtkreader.data.DataStructMon.Companion.LOG_EVENT_MAX
import com.mtkreader.data.DataStructures
import com.mtkreader.data.REL24HCSTR2
import com.mtkreader.data.REL24HCSTR2.Companion.SIZE_24HC_TPAR2
import com.mtkreader.data.reading.*
import com.mtkreader.decodeHex
import com.mtkreader.hasFlag
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.experimental.and

class MonitorServiceImpl : MonitorContract.Service, KoinComponent {

    val context: Context by inject()
    private var dataS: DataStructures = DataStructures()
    var globalIndex = 0
    private val dataMonitor: DataStructMon = DataStructMon()

    fun a() {
        parseMonitor(statusS)
        parseMonitor(eventsS)
        parseMonitor(learnS)
        SaveLogEvent()
    }

    private fun hasErrors(timDate: TimeDate): Boolean {
        val rtcLim = listOf(
            Pair(0x00, 0x59),
            Pair(0x00, 0x59),
            Pair(0x00, 0x23),
            Pair(0x01, 0x07),
            Pair(0x01, 0x31),
            Pair(0x01, 0x12),
            Pair(0x00, 0x99)
        )
        for ((i, tim) in timDate.getArray().withIndex()) {
            if (isBCD(tim) || tim < rtcLim[i].first || tim > rtcLim[i].second)
                return true
        }
        return false
    }

    private fun isBCD(byte: Byte): Boolean {
        return !((byte.toInt() shr 4) < 10 && (byte.toInt() and 0x0F) < 10)
    }

    //---------------------------------------
    //---------------------------PARSE MONITOR
    //---------------------------------------

    var m_dbufLen = 0
    var LearnedDaysRel = Array(4) { Array(7) { false } }
    var m_24cikN = ""
    var m_KVUt = 0
    private var m_SWVerPri = 98     //TODO remove hardcode
    private var m_HWVerPri = Const.Data.TIP_PS


    var m_BrPrekR1 = 0
    var m_BrPrekR2 = 0
    var m_BrPrekR3 = 0
    var m_BrPrekR4 = 0

    var m_AktRel = 0
    var m_PriMod = false


    val statusS = """
00(18041307250421)
08(747374330000000000000000000000000000)
03(E300)
20(0000000000000000)
10(000000000000000000000000000000000000)
12(0A400000)
11(00001100)
22(0001000CCF93)
25(00000000)
26(60616040)
27(00)
28(00)
29(0000000000000000)
14(0000)
"""
    val eventsS = """
05(0001CC00)
06(05006400)
80(34541207250421100001000000000000425412072504211000020000000000004155120725042101004000000000000041561207250421020040000000000000)
81(41571207250421030040000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
82(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
83(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
84(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
85(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
86(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
87(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
88(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
89(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
8A(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
8B(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
8C(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
8D(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
8E(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
8F(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
90(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
91(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
92(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
93(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
94(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
95(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
96(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
97(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
98(00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
"""
    val learnS = """
2A(00FF000000000000FFFF0000000000000000000000000000000000000000)
30(F0007F21C2942D03C03FC4EC5281E0800800800800800800800800800800800800800800)
2B(00FF001000000000FFFF0000000000000000000000000000000000000000)
58(F0007F21C2942D03C03FC4EC5281E0800800800800800800800800800800800800800800)
50(F0007F21C2942D03C03FC4EC5281E0800800800800800800800800800800800800800800)
59(000000800800800800800800800800800800800800800800800800800800800800800800)
51(000000800800800800800800800800800800800800800800800800800800800800800800)
5A(000000800800800800800800800800800800800800800800800800800800800800800800)
52(000000800800800800800800800800800800800800800800800800800800800800800800)
5B(000000800800800800800800800800800800800800800800800800800800800800800800)
53(000000800800800800800800800800800800800800800800800800800800800800800800)
5C(000000800800800800800800800800800800800800800800800800800800800800800800)
54(000000800800800800800800800800800800800800800800800800800800800800800800)
5D(000000800800800800800800800800800800800800800800800800800800800800800800)
55(000000800800800800800800800800800800800800800800800800800800800800800800)
5E(000000800800800800800800800800800800800800800800800800800800800800800800)
56(000000800800800800800800800800800800800800800800800800800800800800800800)
2C(00FF001000000000FFFF0000000000000000000000000000000000000000)
68(C0007F03C16830C3C0800800800800800800800800800800800800800800800800800800)
60(C0007F03C16830C3C0800800800800800800800800800800800800800800800800800800)
69(000000800800800800800800800800800800800800800800800800800800800800800800)
61(000000800800800800800800800800800800800800800800800800800800800800800800)
6A(000000800800800800800800800800800800800800800800800800800800800800800800)
62(000000800800800800800800800800800800800800800800800800800800800800800800)
6B(000000800800800800800800800800800800800800800800800800800800800800800800)
63(000000800800800800800800800800800800800800800800800800800800800800800800)
6C(000000800800800800800800800800800800800800800800800800800800800800800800)
64(000000800800800800800800800800800800800800800800800800800800800800800800)
6D(000000800800800800800800800800800800800800800800800800800800800800800800)
65(000000800800800800800800800800800800800800800800800800800800800800800800)
6E(000000800800800800800800800800800800800800800800800800800800800800800800)
66(000000800800800800800800800800800800800800800800800800800800800800800800)
"""


    override fun parseMonitor(str: String): DataStructMon {
        LearnedDaysRel = Array(4) { Array(7) { false } }
        m_AktRel = 0xE0
        m_PriMod = false    //Mod HDO
        m_24cikN = ""
        dataS.mCfg.cID = 130 //TODO maknuti harcode init na startu

        for (i in str.split("\n")) {
            var x = i.split("(", ")")
            if (x.count() > 2) {
                Log.i(Const.Logging.MONITOR, "\n" + x[0] + " " + x[1])
                var db = x[1].decodeHex()
                var mn = x[0].decodeHex()

                GetLineDat(db, mn[0].toInt())
            }
        }
        return dataMonitor
    }

    private fun GetLineDat(dbuf: ByteArray, monadr: Int) {
        //----
        //var dbuf = ByteArray(128)
        //var monadr = 0
        globalIndex = 0
        when ((monadr and 0xE0.toInt())) {
            0x80 -> {
                UpRamEventLog(monadr and 0x1F.toInt(), dbuf); return
            }
        }
        when ((monadr and 0xF0.toInt())) {
            0xC0 -> ""//UpRamTlgLog
            0x40 -> GetProg7DRx(dbuf, monadr and 0x0F.toInt(), 0)
            0x50 -> GetProg7DRx(dbuf, monadr and 0x0F.toInt(), 1)
            0x60 -> GetProg7DRx(dbuf, monadr and 0x0F.toInt(), 2)

        }
        when (monadr) {
            0x00 -> UpTimPri(dbuf)
            0X04 -> UpWTimPri(dbuf)
            0X03 -> UpAktRel(dbuf)
            0X09 -> GetPrijFlg(dbuf)

            0X08 -> UpProgFile(dbuf)

            // 0x21->UpmBroPre(dbuf)
            0x20 -> UpEEBroPre(dbuf)
            0x10 -> UpZadTeleg(dbuf)
            0x11 -> UpUtf(dbuf)
            0x12 -> UpKVUf(dbuf)
//
            0x22 -> UpBrTstF(dbuf)
            0X05 -> GetDevSerNr(dbuf)
            0X06 -> GetEventH(dbuf)
            // 0X07->GetTlgH(dbuf)
//
            0x25 -> GetRelEvents(dbuf)
            0x26 -> GetRelStatus(dbuf)
            0x27 -> GetPrijEvents(dbuf)
            0x28 -> GetPrijStatus(dbuf)
            0x29 -> GetAktFnRx(dbuf)

            0x2A, 0x2B, 0x2C, 0x2D -> Get24HLearn(dbuf, monadr - 0x2A)
            0x30, 0x31, 0x32, 0x33 -> GetProg24h(dbuf, monadr and 0x7)
            0x14 -> GetRTCFlags(dbuf)
        }


    }

    private fun GetPrijFlg(dbuf: ByteArray) {
        var b1 = dbuf[globalIndex++]
        var b2 = dbuf[globalIndex++]
        var str = String.format("b1 %02x b2 %02x", b1, b2)
    }


    private fun Get24HLearn(dbuf: ByteArray, rel: Int) {

        var mTab24HCX: REL24HCSTR2 = REL24HCSTR2()
        val uk7D = if (dataS.mCfg.cID >= 100) true else false
        for (i in 0 until 7) LearnedDaysRel[rel][i] = false
        if (uk7D) {
            mTab24HCX.Sta7DC.currDay = dbuf[globalIndex++].toInt()
            mTab24HCX.Sta7DC.StartDay = dbuf[globalIndex++].toInt()
            mTab24HCX.Sta7DC.BrUpProg = dbuf[globalIndex++].toInt()
            mTab24HCX.Sta7DC.ctl = dbuf[globalIndex++].toInt()

            if (mTab24HCX.Sta7DC.ctl.hasFlag(mTab24HCX.Sta7DC.BrUpProg)) {
                var startD = mTab24HCX.Sta7DC.StartDay;
                for (sh in 0 until mTab24HCX.Sta7DC.BrUpProg) {
                    LearnedDaysRel[rel][startD++] = true;
                    if (startD >= 7) startD = 0;
                }
            }
        }
        mTab24HCX.State24h = dbuf[globalIndex++].toInt()
        mTab24HCX.RelWrPos = dbuf[globalIndex++].toInt()
        mTab24HCX.LastCmd = dbuf[globalIndex++].toInt()
        mTab24HCX.NrTpar = dbuf[globalIndex++].toInt()
        mTab24HCX.Tsta1Off = BtoLEndian(dbuf)

        for (upar in 0 until SIZE_24HC_TPAR2) {
            mTab24HCX.Tpar[upar].ton = BtoLEndian(dbuf).toShort()
            mTab24HCX.Tpar[upar].toff = BtoLEndian(dbuf).toShort()
        }

        //getStringArray(R.array.IDSI_LRN_ST, sday % 8)
        //var SRelWrPosA=arrayOf("NONE", getString(R.string.IDSI_XON),getString(R.string.IDSI_XOFF),getString(R.string.IDSI_XON)+"-"+getString(R.string.IDSI_XOFF))
        //DEBUG ISPIS skip

        var head = ""
        if (uk7D and ((mTab24HCX.Sta7DC.ctl) != 0)) {
            head = String.format(getString(R.string.IDSI_LEARNPERIOD), rel + 1, getString(R.string.IDSCB_LEARING_PERIODR1_1));
            var sdan = context.resources.getStringArray(R.array.a_days)
            if (mTab24HCX.Sta7DC.StartDay != -1) {
                head += String.format("\r\n %s %s", getString(R.string.IDSI_LRN_STARTDAY), sdan[mTab24HCX.Sta7DC.StartDay % 7]);
                head += String.format("\r\n %s %d/7", getString(R.string.IDSI_LRN_LEARNEDDAYS), mTab24HCX.Sta7DC.BrUpProg);
                head += String.format("\r\n %s [%s]", getString(R.string.IDSI_LRN_TPLEARNED), sdan[mTab24HCX.Sta7DC.currDay % 7]);
            } else {
                head += String.format("\r\n %s %s", getString(R.string.IDSI_LRN_STARTDAY), getString(R.string.IDSI_LRN_NOTSTARTED));
            }
        } else {
            head += String.format(getString(R.string.IDSI_LEARNPERIOD), rel + 1, getString(R.string.IDSCB_LEARING_PERIODR1_0));
        }

        var maxP = SIZE_24HC_TPAR2
        var res = ""
        if (mTab24HCX.NrTpar <= SIZE_24HC_TPAR2) maxP = mTab24HCX.NrTpar

        var upar = 0
        var TP = WTONOFF()
        while (upar < maxP) {
            TP = mTab24HCX.Tpar[upar]
            res += String.format("    \r\n    T-a: %02d:%02d T-b: %02d:%02d", TP.ton / 60, TP.ton % 60, TP.toff / 60, TP.toff % 60)
            upar++
        }
        TP = mTab24HCX.Tpar[upar]
        if ((mTab24HCX.RelWrPos == 1) and (mTab24HCX.LastCmd == 1))
            res += String.format("    \r\n    T-a: %02d:%02d T-b: --:--", TP.ton / 60, TP.ton % 60)
        if ((mTab24HCX.RelWrPos == 2) and (mTab24HCX.LastCmd == 2))
            res += String.format("    \r\n    T-a: --:-- T-b: %02d:%02d", TP.toff / 60, TP.toff % 60)
        if (mTab24HCX.Tsta1Off.toShort() >= 0)
            res += String.format("    \r\n    T-a: --:-- T-b: %02d:%02d", mTab24HCX.Tsta1Off / 60, mTab24HCX.Tsta1Off % 60)


        if (res.isBlank()) res = "    \r\n    T-a: --:-- T-b: --:--"
        if (rel == 0) m_24cikN = ""
        m_24cikN += (head + res + "\r\n")

    }

    private fun GetProg7DRx(dbuf: ByteArray, prog: Int, rel: Int) {

        var head = ""

        var ShowPro = true
        if (prog < 8) {    //SP
            head = String.format("    \r\n    " + getString(R.string.IDSI_RELTP), rel + 1);

        } else { //LP
            head = String.format("\r\n" + getString(R.string.IDSI_RRR) + " %d " + getString(R.string.IDSI_TIMEPAIRLRN) + "(%d)", rel + 1, prog - 7);
            if (!LearnedDaysRel[rel][prog - 8]) ShowPro = false
        }
        m_24cikN += head
        if (ShowPro) GetProg(dbuf)
        else m_24cikN += "    \r\n    T-a: --:-- T-b: --:--"
    }

    private fun GetProg24h(dbuf: ByteArray, rel: Int) {
        m_24cikN += String.format("\n\r" + getString(R.string.IDSI_RELTP), rel + 1)
        GetProg(dbuf)
    }

    private fun GetProg(dbuf: ByteArray) {
        var nrTpar = 8//dataS.mCfg.cNpar
        val x = Unitimbyt()
        var pPProg = Opprog()
        x.i = 0
        x.b[1] = dbuf[globalIndex++]
        x.b[0] = dbuf[globalIndex++]
        x.updateI()

        pPProg.AkTim = x.i
        pPProg.DanPr = dbuf[globalIndex++]
        for (i in 0..nrTpar) {
            x.i = 0;
            x.b[2] = dbuf[globalIndex++]
            x.b[1] = dbuf[globalIndex++]
            x.b[0] = dbuf[globalIndex++]
            x.updateI()
            x.updateTB()

            pPProg.TPro[i] = x.t
        }

        val akdani = mutableListOf<String>()
        for (i in 0..7)
            if (pPProg.DanPr.toInt().hasFlag(0x1 shl i))
                akdani.add(context.resources.getStringArray(R.array.a_days)[i])

        var res = ""
        if (pPProg.DanPr.toInt() != 0)
            res = getString(R.string.IDSI_LRN_ACTDAYS) + akdani.joinToString()


        for (i in 0..nrTpar) {
            val TP = pPProg.TPro[i]
            var par = ""
            if ((!TP.bTonb) and (!TP.bToffb))
                par = String.format("    \r\n    T-a: %02d:%02d T-b: %02d:%02d", TP.Ton / 60, TP.Ton % 60, TP.Toff / 60, TP.Toff % 60)
            if ((!TP.bTonb) and (TP.bToffb))
                par = String.format("    \r\n    T-a: %02d:%02d T-b: --:--", TP.Ton / 60, TP.Ton % 60)
            if ((TP.bTonb) and (!TP.bToffb))
                par = String.format("    \r\n    T-a: --:-- T-b: %02d:%02d", TP.Toff / 60, TP.Toff % 60)
            if ((!TP.bTonb) or (!TP.bToffb))
                res += par;
            Log.i(Const.Logging.MONITOR, par)
        }
        if (res.isBlank())
            res = "    \r\n    T-a: --:-- T-b: --:--"
        m_24cikN += res + "\r\n"
    }

    private fun GetRTCFlags(dbuf: ByteArray) {
        var statRTC = dbuf[globalIndex++].toInt()
        var statsync = dbuf[globalIndex++].toInt()
        var str = String.format("\r\nRTC (%02x) SYNC (%d):", statRTC, statsync)

        val rtc = mutableListOf<String>()
        var sync = ""

        if (statRTC.hasFlag(0x04)) rtc.add("OSC FAIL")
        if (statRTC.hasFlag(0x10)) rtc.add("BAT LOW")
        if (statRTC == 0) rtc.add("OK")

        if (statsync == 1) sync = "OK";


        if (m_HWVerPri == Const.Data.TIP_PSB) {
            if (statRTC.hasFlag(0x10)) rtc.add("Vb<2.5V")
            if (statRTC == 0) rtc.add("Vb>2.5V")
        }

        dataMonitor.disp_RTC = rtc.joinToString()
        dataMonitor.disp_RTCsync = sync
        Log.i(Const.Logging.MONITOR, str)
    }

    private fun GetEventH(dbuf: ByteArray) {
        dataMonitor.m_EvLogH.indx = dbuf[globalIndex++]
        dataMonitor.m_EvLogH.start = dbuf[globalIndex++]
        dataMonitor.m_EvLogH.MaxEvent = dbuf[globalIndex++]
        dataMonitor.m_EvLogH.Event = dbuf[globalIndex++]
    }

    private fun GetDevSerNr(dbuf: ByteArray) {
        dataMonitor.disp_serNum = String.format("%d", setOprel4I(dbuf))
    }

    private fun GetAktFnRx(dbuf: ByteArray) {
        var res = "\r\nnGetAktFnRx: "


        for (i in 0..4) {
            var relx = 0x80 shr i

            var flags = setOprelI(dbuf)

            if ((m_HWVerPri == Const.Data.TIP_PSB) and (i == 3)) break;
            if ((m_HWVerPri == Const.Data.TIP_PS) and (i == 3)) break;
            if (m_AktRel.hasFlag(relx)) {
                var str = ""
                if (flags.hasFlag(Const.Data.AKT_FN_DLY)) str += "|FN_DLY";
                if (flags.hasFlag(Const.Data.AKT_FN_WIPER)) str += "|FN_WIPER";
                if (flags.hasFlag(Const.Data.AKT_FN_VRETCI)) str += "|FN_VRETCI";
                if (flags.hasFlag(Const.Data.AKT_FN_VRES)) str += "|FN_VRES";
                if (flags.hasFlag(Const.Data.AKT_FN_VCI)) str += "|FN_VCI";
                if (flags.hasFlag(Const.Data.AKT_FN_VCI2)) str += "|FN_VCI2";
                if (flags.hasFlag(Const.Data.AKT_SET_ON)) str += "|SET_ON";
                if (flags.hasFlag(Const.Data.AKT_SET_OFF)) str += "|SET_OFF";
                if (flags.hasFlag(Const.Data.AKT_POS_ON)) str += "|POS_ON";
                if (flags.hasFlag(Const.Data.AKT_POS_OFF)) str += "|POS_OFF";

                res += String.format(" R%d (%04X%s)", i + 1, flags, str)

                if (m_PriMod == false)
                    dataMonitor.dispR_Wiper[i] = if (flags.hasFlag(Const.Data.AKT_FN_WIPER)) getString(R.string.yes) else getString(R.string.no)

            }
        }
        Log.i(Const.Logging.MONITOR, res)
    }

    private fun GetPrijStatus(dbuf: ByteArray) {
        var res = String.format("\r\nGetPrijStatus:%02x", dbuf[globalIndex++])
        Log.i(Const.Logging.MONITOR, res)
    }

    private fun GetPrijEvents(dbuf: ByteArray) {
        var status = dbuf[globalIndex++].toInt()
        var str = ""
        var str2 = ""
        if (status.hasFlag(Const.Data.PRIJ_EV_EMTLG)) str += "|EMTLG"
        if (status.hasFlag(Const.Data.PRIJ_EV_STIMP)) str += "|STIMP"
        if (status.hasFlag(Const.Data.PRIJ_EV_TLG)) str += "|TLG"
        if (status.hasFlag(Const.Data.PRIJ_EV_SINH)) str += "|SINH "
        if (status.hasFlag(Const.Data.PRIJ_EV_MYTLG)) str += "|MYTLG"
        if (status.hasFlag(Const.Data.PRIJ_EV_RTCST)) str += "|RTCST"
        if (status.hasFlag(Const.Data.PRIJ_EV_RTCOF)) str += "|RTCOF"
        str2 = String.format("\r\nGetPrijEvents: %02X (%s)", status, str);
        Log.i(Const.Logging.MONITOR, str2)
    }

    private fun GetRelStatus(dbuf: ByteArray) {
        var res = "\r\nGetRelStatus: "

        for (i in 0..3) {
            var relx = 0x80 shr i
            var status = dbuf[globalIndex++].toInt()

            if ((m_HWVerPri == Const.Data.TIP_PSB) and (i == 3)) break;
            if ((m_HWVerPri == Const.Data.TIP_PS) and (i == 3)) break;
            if (m_AktRel.hasFlag(relx)) {

                var tstr = ""
                if (status.hasFlag(Const.Data.REL_PROG_UNLOCK)) tstr += "|PROG_UNLOCK";
                if (m_PriMod == false) {
                    if (status.hasFlag(Const.Data.REL_LEARN_EN)) tstr += "|LEARN_EN";
                    if (status.hasFlag(Const.Data.REL_TA_STATE)) tstr += "|TA_STATE";
                    if (status.hasFlag(Const.Data.REL_LEARN_INTR)) tstr += "|LEARN_INTR";
                    if (status.hasFlag(Const.Data.REL_TIMPR_UNLOCK)) tstr += "|TIMPR_UNLOCK";
                    if (status.hasFlag(Const.Data.REL_EM_STATE)) tstr += "|EM_STATE";
                }
                res += String.format(" R%d (%02X%s)", i + 1, status, tstr)


                dataMonitor.dispR_ProgEn[i] = if (status.hasFlag(Const.Data.REL_PROG_UNLOCK)) getString(R.string.yes) else getString(R.string.no)
                if (m_PriMod == false) {

                    if (!status.hasFlag(Const.Data.REL_LEARN_EN)) dataMonitor.dispR_Learn[i] = getString(R.string.IDS_LRN_DIS)
                    else if (!status.hasFlag(Const.Data.REL_LEARN_INTR)) dataMonitor.dispR_Learn[i] = getString(R.string.IDS_LRN_OK)
                    else dataMonitor.dispR_Learn[i] = getString(R.string.IDS_LRN_INTR)

                    if (status.hasFlag(Const.Data.REL_EM_STATE)) dataMonitor.dispR_ProgEn[i] = getString(R.string.HOZ)

                    dataMonitor.dispR_TransmitFail[i] =
                        if (status.hasFlag(Const.Data.REL_TA_STATE)) getString(R.string.yes) else getString(R.string.no)
                    dataMonitor.dispR_LoopEn[i] = if (status.hasFlag(Const.Data.REL_TIMPR_UNLOCK)) getString(R.string.yes) else getString(R.string.no)
                }
            }        //end akt rel;

        }
        Log.i(Const.Logging.MONITOR, res)

    }

    private fun GetRelEvents(dbuf: ByteArray) {
        var res = "\r\nGetRelEvents:"
        for (rel in 0..3) {
            val revents = dbuf[globalIndex++].toInt()
            var tstr = ""
            if (revents.hasFlag(Const.Data.REL_EV_TL_ON)) tstr += "|ON"
            if (revents.hasFlag(Const.Data.REL_EV_TL_OFF)) tstr += "|OFF"
            if (revents.hasFlag(Const.Data.REL_EV_TL_LDIS)) tstr += "|LRNDIS"
            if (revents.hasFlag(Const.Data.REL_EV_TL_LINTR)) tstr += "|LRNINTR"
            if (revents.hasFlag(Const.Data.REL_EV_TL_PROEN)) tstr += "|PROEN"
            if (revents.hasFlag(Const.Data.REL_EV_TL_PRODI)) tstr += "|PRODI"
            res += String.format(" R%d (%02X%s)", rel + 1, revents, tstr)

        }
        Log.i(Const.Logging.MONITOR, res)

    }

    private fun UpBrTstF(dbuf: ByteArray) {
        if (m_SWVerPri >= 90)
            dataMonitor.disp_outage = String.format(" %d", setOprelI(dbuf))
        else
            dataMonitor.disp_outage = String.format(" %d", dbuf[globalIndex++])

        if (dbuf.count() >= 6) {
            val tin = setOprel4I(dbuf)
            val days = tin / (3600 * 24)
            val hh: Int = tin % (3600 * 24) / 3600
            val mm: Int = tin % 3600 / 60
            val ss: Int = tin % 60
            dataMonitor.disp_timeInOp = String.format("%d day %02d:%02d:%02d ", days, hh, mm, ss)


        }
    }

    private fun UpUtf(dbuf: ByteArray) {
        var utf = 0.0
        if (m_SWVerPri >= 90) {
            utf = setOprel4I(dbuf).toDouble() * 1.001
            if (m_KVUt > 0) utf = utf / m_KVUt
            else utf = utf / 0xA20
        } else
            utf = setOprelI(dbuf).toDouble() * 0.0028

        dataMonitor.disp_UTF = if (m_PriMod == false) String.format("%.2f", utf) else ""
    }

    private fun UpKVUf(dbuf: ByteArray) {
        m_KVUt = setOprelI(dbuf)
    }


    private fun GetImpName(nr: Int): String {
        var imp = ""
        String.format("%d", nr)
        when (nr) {
            in 0..4 -> imp = String.format("A%d", nr)
            in 5..12 -> imp = String.format("B%d", nr - 4)
            in 13..44 -> {
                if (nr % 2 == 1) imp = String.format("DP%dz", (nr - 13) / 2 + 1)
                else imp = String.format("DP%dv", (nr - 13) / 2 + 1)
            }
            else -> imp = String.format("C%d", nr - 44)

        }
        return imp
    }

    private fun GetTlgImp(dbuf: ByteArray): String {
        var res = ""
        var cnt = 0
        val imps = mutableListOf<String>()
        for (i in 0..7) {
            for (j in 0..7) {
                if (dbuf[i].toInt() and Const.Data.bVtmask[j].toInt() != 0)
                    imps.add(GetImpName(8 * i + j + 1))

            }

        }
        val tlg = imps.joinToString(separator = ", ")
        return tlg
    }

    private fun UpZadTeleg(dbuf: ByteArray) {

        var str = GetTlgImp(dbuf);

        //std::vector<CString> names = DBQ::GetTlgNamesMatchingRectlg(tlgx);
        //for (auto value : names) str += " - " + value;

        if (m_PriMod == false) str; // ne mjenjaj
        else str = "" // brisi

        dataMonitor.disp_lastTlg = str

    }


    private fun UpEEBroPre(dbuf: ByteArray) {
        m_BrPrekR1 = setOprelI(dbuf)
        m_BrPrekR2 = setOprelI(dbuf)
        m_BrPrekR3 = setOprelI(dbuf)
        m_BrPrekR4 = setOprelI(dbuf)
        if ((m_AktRel and 0x80) != 0) dataMonitor.dispR1_BrPrek = String.format("%d", m_BrPrekR1)
        if ((m_AktRel and 0x40) != 0) dataMonitor.dispR2_BrPrek = String.format("%d", m_BrPrekR2)
        if ((m_AktRel and 0x20) != 0) dataMonitor.dispR3_BrPrek = String.format("%d", m_BrPrekR3)
        if ((m_AktRel and 0x10) != 0) dataMonitor.dispR4_BrPrek = String.format("%d", m_BrPrekR4)

    }

    private fun UpAktRel(dbuf: ByteArray) {


        var b = dbuf[globalIndex++].toInt()
        m_AktRel = b and 0xE0
        //m_PriMod=if(b.hasFlag(0x02)) true else false //TODO ?????  m_PriMod
        dbuf[globalIndex++]

    }

    private fun UpProgFile(dbuf: ByteArray) {
        var paramfile = ""
        for (i in 1 until 18) {
            val c = dbuf[globalIndex++]
            if (c == 0.toByte()) break
            paramfile += c.toChar() //TODO

        }
        dataMonitor.disp_paramfile = paramfile
        Log.i(Const.Logging.MONITOR, paramfile)
    }


    private fun UpWTimPri(dbuf: ByteArray) {
        var res = ""

        var wtime = setOprel3I(dbuf)

        val sec = wtime % 60
        wtime /= 60
        val min = wtime % 60
        wtime /= 60
        val hour = wtime % 24
        wtime /= 24

        res = String.format("%s-%02d:%02d:%02d", context.resources.getStringArray(R.array.a_days)[wtime], hour, min, sec)
        dataMonitor.disp_time = res
        Log.i(Const.Logging.MONITOR, res)

    }

    private fun UpTimPri(dbuf: ByteArray) {
        var res = ""

        val timeDate = TimeDate(dbuf[0], dbuf[1], dbuf[2], dbuf[3], dbuf[4], dbuf[5], dbuf[6])
        timeDate.dan = timeDate.dan and 0x0F

        var isInvalid = hasErrors(timeDate)

        timeDate.sek = timeDate.sek and 0x7F

        if (timeDate.dan in 1..7) timeDate.dan =
            (timeDate.dan.toInt() - 1).toByte() else isInvalid = true
        var time = ""
        var date = ""

        if (isInvalid) {
            time = "? : ? : ?"
            date = "? - ? - ? - ?"
            res = date + time
            dataMonitor.disp_time = time
            dataMonitor.disp_date = date
            //return res
        } else {
            time = String.format(
                context.getString(R.string.day_time_format),
                context.resources.getStringArray(R.array.a_days)[timeDate.dan.toInt()],
                timeDate.sat,
                timeDate.min,
                timeDate.sek
            )
            //if (hardwareVersion == Const.Data.TIP_PA)
            date = String.format(
                context.getString(R.string.date_time_format),
                timeDate.dat,
                timeDate.mje,
                timeDate.god
            )
            if (true) res = date + time //TODO add hw version TIP_PS || TIP_PSB
            else res = time
        }
        dataMonitor.disp_time = time
        dataMonitor.disp_date = date
        Log.i(Const.Logging.MONITOR, res)
        //return Pair(time, date)


    }

    fun SaveLogEvent() {
        var csvStr = getString(R.string.IDSC_TIME) + ";" +
                getString(R.string.IDSTOC_EVENTLOG) + ";" +
                getString(R.string.IDSI_SERIALNUM) + dataMonitor.disp_serNum + "\r\n"
        var res = ""
        var rindx = 0;
        var endidx = 0;
        //CListBox *loglist=(CListBox*)GetDlgItem(IDC_LOGEVENT);
        //loglist->ResetContent();

        if (dataMonitor.m_EvLogH.start > 0 || dataMonitor.m_EvLogH.indx > 0) {
            if (dataMonitor.m_EvLogH.start < dataMonitor.m_EvLogH.indx) //nije napravio krug
            {
                rindx = dataMonitor.m_EvLogH.start.toInt()
                endidx = dataMonitor.m_EvLogH.indx.toInt()
            } else {
                rindx = dataMonitor.m_EvLogH.indx.toInt()
                endidx = dataMonitor.m_EvLogH.indx.toInt()
            }


            do {
                var day = dataMonitor.m_EvLog[rindx].Time.day
                var sdan = context.resources.getStringArray(R.array.a_days)
                var strday = if (day > 0) sdan[day - 1] else "???"
                var obj = dataMonitor.m_EvLog[rindx].Obj
                var event = ""

                when (obj) {
                    0x80 -> event = "Telegram:" + GetTlgImp(dataMonitor.m_EvLog[rindx].Event.Imp) //SNO_TLG
                    0xA0 -> event = "Telegram:" + GetTlgString(dataMonitor.m_EvLog[rindx].Event.Imp) //HOZ
                    0xC0 -> event = "Telegram:" + GetTlgString(dataMonitor.m_EvLog[rindx].Event.Imp) //SYNC
                    else -> {
                        var name = (dataMonitor.m_EvLog[rindx].Event.bH shl 8) or (dataMonitor.m_EvLog[rindx].Event.bL)
                        event = GetEventString(obj, name)
                    }
                }

                var datum = String.format(
                    "%02X-%02X-%02X",
                    dataMonitor.m_EvLog[rindx].Time.dat,
                    dataMonitor.m_EvLog[rindx].Time.month,
                    dataMonitor.m_EvLog[rindx].Time.year
                );
                var vrijeme = String.format(
                    "%02X:%02X:%02X",
                    dataMonitor.m_EvLog[rindx].Time.hour,
                    dataMonitor.m_EvLog[rindx].Time.min,
                    dataMonitor.m_EvLog[rindx].Time.sec
                );

                vrijeme += strday;
                vrijeme = datum + " " + vrijeme;

                res += String.format("%s %s\r\n", vrijeme, event)
                csvStr += String.format("%s;%s\r\n", vrijeme, event);
                //loglist->AddString(str);
                //pFrameWnd->ShowData(str);

                rindx++;
                if (rindx >= LOG_EVENT_MAX) {
                    rindx = 0;
                }
            } while (rindx != endidx);
        }
        dataMonitor.disp_eventlog = res
        dataMonitor.disp_eventlogCSV = csvStr
    }

    private fun GetTlgString(tlg: ByteArray): String {
        //CString str = _T("");
        //std::vector<CString> names = DBQ::GetTlgNamesMatchingRectlg(tlgx);
        //for (auto value : names) str += " - "+ value;
        //if(str.GetLength() <= 0)
        var str = GetTlgImp(tlg);
        return str;

    }

    private fun GetEventString(obj: Int, name: Int): String {
        var r = ""
        when (obj) {
            SNO_RTC -> {
                if (name == SNE_RTC_ST) r = getString(R.string.IDS_EVL_RTC_ST)
                else if (name == SNE_RTC_OF) r = getString(R.string.IDS_EVL_RTC_OF)
                else if (name == SNE_RTC_BL) r = getString(R.string.IDS_EVL_RTC_BL)
                else if (name == SNE_RTC_OOK) r = getString(R.string.IDS_EVL_RTC_OOK)
                else r = String.format("RTC:%04X", name)

            }
            SNO_REL1, SNO_REL2, SNO_REL3, SNO_REL4 -> {
                var swr = ""
                if (name.hasFlag(PRO_REL_X)) swr = getString(R.string.IDS_EVL_PRO_REL_X)
                if (name.hasFlag(TEL_REL_X)) swr = getString(R.string.IDS_EVL_TEL_REL_X)
                if (name.hasFlag(CLP_REL_X)) swr = getString(R.string.IDS_EVL_CLP_REL_X)
                if (name.hasFlag(REL_WIP_R)) swr = getString(R.string.IDS_EVL_WIP_REL_X)
                if (name.hasFlag(REL_WIP_S)) swr = getString(R.string.IDS_EVL_TEL_REL_X) + " " + getString(R.string.IDS_EVL_WIP_REL_X)
                if (name.hasFlag(PON_REL_X)) swr = getString(R.string.IDS_EVL_PWR_REL_X)
                if (name.hasFlag(EMT_REL_X)) swr = getString(R.string.IDS_EVL_TEL_REL_X) + " H"

                var tr: String
                if (name.hasFlag(REL_ON)) tr = getString(R.string.IDS_EVL_ON) + " " + swr
                else if (name.hasFlag(REL_OFF)) tr = getString(R.string.IDS_EVL_OFF) + " " + swr
                else if (name == REL_PROBLOCK) tr = getString(R.string.IDS_EVL_PROBLOCK)
                else if (name == REL_PROUNBLOCK) tr = getString(R.string.IDS_EVL_PROUNBLOCK)
                else if (name == REL_TA_S) tr = getString(R.string.IDS_EVL_TA_S)
                else if (name == REL_TA_R) tr = getString(R.string.IDS_EVL_TA_R)
                else tr = String.format("%04X", name)
                r = String.format("R%d:", obj) + tr

            }
            SNO_PRIJEM -> {
                when (name) {
                    SNE_POFF -> r = getString(R.string.IDS_EVL_POFF)
                    SNE_PON -> r = getString(R.string.IDS_EVL_PON)
                    SNE_SHT -> r = getString(R.string.IDS_EVL_SHT)
                    SNE_SHD -> r = getString(R.string.IDS_EVL_SHD)
                    SNE_LSINH -> r = getString(R.string.IDS_EVL_LSINH)
                    SNE_WPAROK -> r = getString(R.string.IDS_EVL_WPAROK)
                    SNE_WPARERR -> r = getString(R.string.IDS_EVL_WPARERR)
                    else -> r = String.format("RECEIVER:%02X", name)
                }
            }
            else -> r = String.format("UNKNOWN:%02X,%04X", obj, name)

        }
        return r;
    }

    fun UpRamEventLog(nr: Int, dbuf: ByteArray) {
        val cnt = nr * 4
        if (cnt >= DataStructMon.LOG_EVENT_MAX) return

        for (i in (cnt)..(cnt + 3)) {

            dataMonitor.m_EvLog[i].Time.sec = dbuf[globalIndex++].toInt()
            dataMonitor.m_EvLog[i].Time.min = dbuf[globalIndex++].toInt()
            dataMonitor.m_EvLog[i].Time.hour = dbuf[globalIndex++].toInt()
            dataMonitor.m_EvLog[i].Time.day = dbuf[globalIndex++].toInt()
            dataMonitor.m_EvLog[i].Time.dat = dbuf[globalIndex++].toInt()
            dataMonitor.m_EvLog[i].Time.month = dbuf[globalIndex++].toInt()
            dataMonitor.m_EvLog[i].Time.year = dbuf[globalIndex++].toInt()
            dataMonitor.m_EvLog[i].Obj = dbuf[globalIndex++].toInt()

            var tmp = dbuf[globalIndex++]
            dataMonitor.m_EvLog[i].Event.bH = tmp.toInt()
            dataMonitor.m_EvLog[i].Event.Imp[0] = tmp

            tmp = dbuf[globalIndex++]
            dataMonitor.m_EvLog[i].Event.bL = tmp.toInt()
            dataMonitor.m_EvLog[i].Event.Imp[1] = tmp

            dataMonitor.m_EvLog[i].Event.Imp[2] = dbuf[globalIndex++]
            dataMonitor.m_EvLog[i].Event.Imp[3] = dbuf[globalIndex++]
            dataMonitor.m_EvLog[i].Event.Imp[4] = dbuf[globalIndex++]
            dataMonitor.m_EvLog[i].Event.Imp[5] = dbuf[globalIndex++]
            dataMonitor.m_EvLog[i].Event.Imp[6] = dbuf[globalIndex++]
            dataMonitor.m_EvLog[i].Event.Imp[7] = dbuf[globalIndex++]

        }
    }


    //////////////// UTILITY
    private fun getStringArray(resId: Int, index: Int): String {
        return context.resources.getStringArray(resId)[index]
    }

    private fun getString(resId: Int): String {
        return context.resources.getString(resId)
    }

    private fun setOprelI(dbuf: ByteArray)
            : Int {
        val b1 = dbuf[globalIndex++]
        val b0 = dbuf[globalIndex++]
        val tempi = Uni4byt(byteArrayOf(b0, b1, 0, 0))
        return tempi.i
    }

    private fun setOprel3I(dbuf: ByteArray)
            : Int {
        val b2 = dbuf[globalIndex++]
        val b1 = dbuf[globalIndex++]
        val b0 = dbuf[globalIndex++]
        val tempi = Uni4byt(byteArrayOf(b0, b1, b2, 0))
        return tempi.i
    }

    private fun setOprel4I(dbuf: ByteArray)
            : Int {
        val b3 = dbuf[globalIndex++]
        val b2 = dbuf[globalIndex++]
        val b1 = dbuf[globalIndex++]
        val b0 = dbuf[globalIndex++]
        val tempi = Uni4byt(byteArrayOf(b0, b1, b2, b3))
        return tempi.i
    }

    private fun BtoLEndian(dbuf: ByteArray): Int {
        //UNW2BYT tempw;
        //tempw.w=0;
        //tempw.b[1]=*m_pbbuf++;
        //tempw.b[0]=*m_pbbuf++;
        //return(tempw.w);


        val b1 = dbuf[globalIndex++]
        val b0 = dbuf[globalIndex++]
        val tempi = Uni4byt(byteArrayOf(b0, b1, 0, 0))
        return tempi.i
    }


}
