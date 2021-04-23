package com.mtkreader.services

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.contracts.TimeContract
import com.mtkreader.data.DataStructMon
import com.mtkreader.data.DataStructures
import com.mtkreader.data.DeviceDate
import com.mtkreader.data.DeviceTime
import com.mtkreader.data.reading.Opprog
import com.mtkreader.data.reading.TimeDate
import com.mtkreader.data.reading.Uni4byt
import com.mtkreader.data.reading.Unitimbyt
import com.mtkreader.data.writing.DataRXMessage
import com.mtkreader.data.writing.DataTXTMessage
import com.mtkreader.hasFlag
import com.mtkreader.utils.CommunicationUtil
import com.mtkreader.utils.DataUtils
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

class MonitorServiceImpl : TimeContract.Service,KoinComponent {
    val context: Context by inject()
    private var dataS: DataStructures = DataStructures()
    private lateinit var socket: BluetoothSocket
    private var readMessageData = DataRXMessage()
    private val data = mutableListOf<Char>()
    var globalIndex=0
    private val datmon: DataStructMon = DataStructMon()
    override fun setSocket(socket: BluetoothSocket) {
        this.socket = socket
    }

    override fun extractTimeData(
        context: Context,
        data: List<Char>,
        hardwareVersion: Int
    ): Single<Pair<String, String>> {
        return Single.fromCallable<Pair<String, String>> {
            val dbuf = parseTimeData(data)
            return@fromCallable generateTimeString(context, dbuf, hardwareVersion)
        }
    }

    override fun setTimeDate(time: DeviceTime, deviceDate: DeviceDate): Single<Boolean> {
        return Single.fromCallable<Boolean> {
            return@fromCallable startTimeWrite(deviceDate, time)
        }
    }


    private fun parseTimeData(data: List<Char>): ByteArray {
        var i = 1
        var monadr = 0
        var bb: Char
        var mDaterr = 0

        while (i < 4) {
            if (data[i] == '(') break
            bb = DataUtils.HtoB(data[i++])
            if (bb >= 0.toChar()) {
                monadr = monadr shl 4
                monadr = monadr or bb.toInt()
            } else mDaterr++
        }

        if (data[i++] != '(') mDaterr++
        val dbuf = ByteArray(128)
        var k: Int

        for (j in 0..(data.size / 2 + 1)) {
            k = 2
            while (k-- != 0) {
                if (data[i] == ')') break
                bb = DataUtils.HtoB(data[i++])

                if (bb >= 0.toChar()) {
                    dbuf[j] = (dbuf[j].toInt() shl 4).toByte()
                    dbuf[j] = dbuf[j] or bb.toByte()
                } else mDaterr++
            }
            if (data[i] == ')') break
        }
        return dbuf
    }

    private fun generateTimeString(
        context: Context,
        dbuf: ByteArray,
        hardwareVersion: Int
    ): Pair<String, String> {
        val timeDate = TimeDate(dbuf[0], dbuf[1], dbuf[2], dbuf[3], dbuf[4], dbuf[5], dbuf[6])
        timeDate.dan = timeDate.dan and 0x0F

        var isInvalid = hasErrors(timeDate)

        timeDate.sek = timeDate.sek and 0x7F

        if (timeDate.dan in 1..7) timeDate.dan =
            (timeDate.dan.toInt() - 1).toByte() else isInvalid = true

        if (isInvalid) {
            val wrongFormat = String.format(
                context.getString(R.string.wrong_value_time),
                timeDate.dan,
                timeDate.sat,
                timeDate.min,
                timeDate.sek,
                timeDate.dat,
                timeDate.mje,
                timeDate.god
            )
            return Pair(wrongFormat, wrongFormat)
        }
        val time = String.format(
            context.getString(R.string.day_time_format),
            context.resources.getStringArray(R.array.a_days)[timeDate.dan.toInt()],
            timeDate.sat,
            timeDate.min,
            timeDate.sek
        )
        //if (hardwareVersion == Const.Data.TIP_PA)
        val date = String.format(
            context.getString(R.string.date_time_format),
            timeDate.dat,
            timeDate.mje,
            timeDate.god
        )
        return Pair(time, date)
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


    private fun startTimeWrite(deviceDate: DeviceDate, time: DeviceTime): Boolean {
        val timeDate = TimeDate()
        val year = (deviceDate.year % 100).toByte()
        with(timeDate) {
            god = ((year / 10) shl 4).toByte()
            god = god or ((year % 10).toByte())

            dat = ((deviceDate.day / 10) shl 4).toByte()
            dat = dat or ((deviceDate.day % 10).toByte())

            mje = (((deviceDate.month + 1) / 10) shl 4).toByte()
            mje = mje or (((deviceDate.month + 1) % 10).toByte())

            sat = ((time.hours / 10) shl 4).toByte()
            sat = sat or ((time.hours % 10).toByte())

            min = ((time.minutes / 10) shl 4).toByte()
            min = min or ((time.minutes % 10).toByte())

            sek = ((time.seconds / 10) shl 4).toByte()
            sek = sek or (time.seconds % 10).toByte()
            val cal = GregorianCalendar(deviceDate.year, deviceDate.month, deviceDate.day - 1)

            dan = cal.get(GregorianCalendar.DAY_OF_WEEK).toByte()
            return writeTime(timeDate)
        }
    }

    private fun writeTime(timeDate: TimeDate): Boolean {
        val timeString = String.format(
            Const.Data.TIME_FORMAT,
            timeDate.sek,
            timeDate.min,
            timeDate.sat,
            timeDate.dan
        )
        val timeDateString = String.format(
            Const.Data.TIME_DATE_FORMAT,
            timeDate.sek,
            timeDate.min,
            timeDate.sat,
            timeDate.dan,
            timeDate.dat,
            timeDate.mje,
            timeDate.god
        )

        return !waitAnswer(timeDateString)

    }

    private fun waitAnswer(time: String): Boolean {
        var isSuccessful = false
        loop@ for (i in 1..3) {
            sendStringToDevice(time)

            readMessageData = DataRXMessage()
            if (waitMessage()) {
                when (readMessageData.status) {
                    Const.Data.ACK -> {
                        isSuccessful = true
                        break@loop
                    }
                    Const.Data.NAK -> continue@loop
                    Const.Data.COMPLETE -> break@loop

                }
            }

        }
        readMessageData = DataRXMessage()
        return isSuccessful
    }

    override fun setReadData(data: List<Char>) {
        this.data.clear()
        this.data.addAll(data)
    }

    private fun waitMessage(): Boolean {
        val timeOut = System.currentTimeMillis() + 1500
        do {
            if (System.currentTimeMillis() > timeOut) {
                println("Timed out!")
                return false
            }

        } while (!endOfMessage())
        return true
    }

    private fun endOfMessage(): Boolean {
        while (true) {
            if (data.isNotEmpty()) {
                for (dataByte in data) {
                    if (readMessageData.status == Const.Data.ETX || readMessageData.status == Const.Data.EOT) {
                        readMessageData.bcc = readMessageData.bcc xor dataByte.toByte()
                        if (readMessageData.bcc == 0.toByte()) {
                            readMessageData.status = Const.Data.COMPLETE
                        } else {
                            readMessageData.proterr = 0xCC.toByte()
                        }
                        return true
                    } else {
                        if (readMessageData.status == Const.Data.SOH || readMessageData.status == Const.Data.STX)
                            readMessageData.bcc = readMessageData.bcc xor dataByte.toByte()

                        when (dataByte.toByte()) {
                            Const.Data.SOH -> {
                                if (readMessageData.count == 0)
                                    readMessageData.status = Const.Data.SOH
                                else
                                    readMessageData.proterr = Const.Data.SOH
                                readMessageData.buffer[readMessageData.count++] = dataByte.toByte()
                            }
                            Const.Data.STX -> {
                                if (readMessageData.status == Const.Data.SOH || readMessageData.count == 0)
                                    readMessageData.status = Const.Data.STX
                                else
                                    readMessageData.proterr = Const.Data.STX
                                readMessageData.buffer[readMessageData.count++] = dataByte.toByte()
                            }
                            0x0D.toByte() -> {
                                readMessageData.buffer[readMessageData.count++] = dataByte.toByte()
                                if (readMessageData.type == 0.toByte()) readMessageData.crlf = 0x0D
                            }
                            0x0A.toByte() -> {
                                readMessageData.buffer[readMessageData.count++] = dataByte.toByte()
                                if (readMessageData.type == 0.toByte()) {
                                    if (readMessageData.crlf == 0x0D.toByte())
                                        readMessageData.crlf = 0x0A
                                    readMessageData.status = Const.Data.COMPLETE
                                    return true
                                }
                            }
                            Const.Data.ETX -> {
                                readMessageData.status = Const.Data.ETX
                            }
                            Const.Data.EOT -> {
                                readMessageData.status = Const.Data.EOT
                            }
                            Const.Data.ACK -> {
                                readMessageData.status = Const.Data.ACK
                                return true
                            }
                            Const.Data.NAK -> {
                                readMessageData.status = Const.Data.NAK
                                return true
                            }
                            else -> {
                                readMessageData.buffer[readMessageData.count++] = dataByte.toByte()
                                if (readMessageData.count > 2048 * 4) {
                                    readMessageData.proterr = 0x55
                                    return false
                                }
                            }

                        }
                    }
                }
            }


            Thread.sleep(700)
        }
    }

    private fun sendStringToDevice(time: String) {
        var j = 0
        val messageSendData = DataTXTMessage()
        if (time.isNotEmpty()) {
            messageSendData.buffer[j++] = Const.Data.SOH
            for (char in time) {
                messageSendData.buffer[j++] = char.toByte()
                messageSendData.bcc = messageSendData.bcc xor char.toByte()
            }
            messageSendData.buffer[j++] = Const.Data.ETX
            messageSendData.bcc = messageSendData.bcc xor Const.Data.ETX
            messageSendData.buffer[j++] = messageSendData.bcc
            messageSendData.count = j

            CommunicationUtil.writeToSocket(
                socket,
                messageSendData.buffer.take(messageSendData.count).toByteArray()
            )
        }
    }


    //---------------------------------------
    //---------------------------PARSE MONITOR
    //---------------------------------------

    var m_dbufLen=0

    private fun GetLineDat(){
        //----
        var dbuf=ByteArray(128)
        var monadr=0
        when((monadr and 0xE0.toInt())){
             0x80 -> {UpRamEventLog(monadr and 0x1F.toInt(),dbuf); return}
        }
        when((monadr and 0xF0.toInt())) {
            0xC0-> ""//UpRamTlgLog
            0x40->GetProg7DRx(dbuf,monadr and 0xF0.toInt(),0)
            0x50->GetProg7DRx(dbuf,monadr and 0xF0.toInt(),1)
            0x60->GetProg7DRx(dbuf,monadr and 0xF0.toInt(),2)

        }
        when(monadr){
            0x00->UpTimPri(dbuf)
            0X04->UpWTimPri(dbuf)
            0X03->UpAktRel(dbuf)
          //  0X09->GetPrijFlg(dbuf)

            0X08->UpProgFile(dbuf)

           // 0x21->UpmBroPre(dbuf)
            0x20->UpEEBroPre(dbuf)
            0x10->UpZadTeleg(dbuf)
           0x11->UpUtf(dbuf)
           0x12->UpKVUf(dbuf)
//
           0x22->UpBrTstF(dbuf)
           0X05->GetDevSerNr(dbuf)
            0X06->GetEventH(dbuf)
           // 0X07->GetTlgH(dbuf)
//
           0x25->GetRelEvents(dbuf)
           0x26->GetRelStatus(dbuf)
            0x27->GetPrijEvents(dbuf)
            0x28->GetPrijStatus(dbuf)
            0x29->GetAktFnRx(dbuf)

            //0x2A, 0x2B, 0x2C, 0x2D->Get24HLearn(dbuf,monadr-0x2A)
            0x30, 0x31, 0x32, 0x33->GetProg24h(dbuf, monadr and 0x7)
            0x14->GetRTCFlags(dbuf)
        }


    }

    private fun GetProg24h(dbuf: ByteArray, rel: Int) {
        var nrTpar = 8//dataS.mCfg.cNpar
        val x = Unitimbyt()
        var pPProg = Opprog()
        x.i=0
        x.b[1]=dbuf[globalIndex++]
        x.b[0]=dbuf[globalIndex++]
        x.updateI()

        pPProg.AkTim=x.i
        pPProg.DanPr=dbuf[globalIndex++]
        for(i in 0..nrTpar)
        {
            x.i = 0;
            x.b[2] = *pdbuf++;
            x.b[1] = *pdbuf++;
            x.b[0] = *pdbuf++;
            x.updateI()
            pPProg.AkTim
        }

        context.resources.getStringArray(R.array.a_days)[i]
    }

    private fun GetRTCFlags(dbuf: ByteArray) {
        var statRTC=dbuf[globalIndex++].toInt()
        var statsync=dbuf[globalIndex++].toInt()
        var str=String.format("\r\nRTC (%02x) SYNC (%d):",statRTC,statsync)

        val rtc = mutableListOf<String>()
        var sync=""

        if (statRTC.hasFlag(0x04)) rtc.add("OSC FAIL")
        if (statRTC.hasFlag(0x10)) rtc.add("BAT LOW")
        if(statRTC==0) rtc.add("OK")

        if (statsync == 1) sync = "OK";


        if (m_HWVerPri == Const.Data.TIP_PSB){
            if (statRTC.hasFlag(0x10))rtc.add("Vb<2.5V")
            if (statRTC ==0) rtc.add("Vb>2.5V")
        }

        datmon.disp_RTC=rtc.joinToString()
        datmon.disp_RTCsync=sync

    }

    private fun GetEventH(dbuf: ByteArray) {
        datmon.m_EvLogH.indx=dbuf[globalIndex++]
        datmon.m_EvLogH.start=dbuf[globalIndex++]
        datmon.m_EvLogH.MaxEvent=dbuf[globalIndex++]
        datmon.m_EvLogH.Event=dbuf[globalIndex++]
    }

    private fun GetDevSerNr(dbuf: ByteArray) {
        datmon.disp_serNum=String.format("%d",setOprel4I(dbuf))
    }

    private fun GetAktFnRx(dbuf: ByteArray) {
        var res = "\r\nnGetAktFnRx: "


        for (i in 0..4) {
            var relx = 0x80 shr i

            var flags=setOprelI(dbuf)

            if ((m_HWVerPri == Const.Data.TIP_PSB) and (i == 3)) break;
            if ((m_HWVerPri == Const.Data.TIP_PS) and (i == 3)) break;
            if (m_AktRel.hasFlag(relx)) {
                var str = ""
                if (flags.hasFlag(Const.Data.AKT_FN_DLY))str += "|FN_DLY";
                if (flags.hasFlag(Const.Data.AKT_FN_WIPER))str += "|FN_WIPER";
                if (flags.hasFlag(Const.Data.AKT_FN_VRETCI))str += "|FN_VRETCI";
                if (flags.hasFlag(Const.Data.AKT_FN_VRES))str += "|FN_VRES";
                if (flags.hasFlag(Const.Data.AKT_FN_VCI))str += "|FN_VCI";
                if (flags.hasFlag(Const.Data.AKT_FN_VCI2))str += "|FN_VCI2";
                if (flags.hasFlag(Const.Data.AKT_SET_ON))str += "|SET_ON";
                if (flags.hasFlag(Const.Data.AKT_SET_OFF))str += "|SET_OFF";
                if (flags.hasFlag(Const.Data.AKT_POS_ON))str += "|POS_ON";
                if (flags.hasFlag(Const.Data.AKT_POS_OFF))str += "|POS_OFF";

                res += String.format(" R%d (%04X%s)", i + 1, flags, str)

                if (m_PriMod == false)
                    datmon.dispR_Wiper[i] = if (flags.hasFlag(Const.Data.AKT_FN_WIPER)) getString(R.string.yes) else getString(R.string.no) //TODO wiper ili loop

            }
        }
    }

    private fun GetPrijStatus(dbuf: ByteArray) {
        var res=String.format("\r\nGetPrijStatus:%02x",dbuf[globalIndex++])
    }

    private fun GetPrijEvents(dbuf: ByteArray) {
        var status = dbuf[globalIndex++].toInt()
        var str =""
        var str2 =""
        if(status.hasFlag(Const.Data.PRIJ_EV_EMTLG))	str+="|EMTLG"
        if(status.hasFlag(Const.Data.PRIJ_EV_STIMP))	str+="|STIMP"
        if(status.hasFlag(Const.Data.PRIJ_EV_TLG))      str+="|TLG"
        if(status.hasFlag(Const.Data.PRIJ_EV_SINH))	    str+="|SINH "
        if(status.hasFlag(Const.Data.PRIJ_EV_MYTLG))	str+="|MYTLG"
        if(status.hasFlag(Const.Data.PRIJ_EV_RTCST))	str+="|RTCST"
        if(status.hasFlag(Const.Data.PRIJ_EV_RTCOF))	str+="|RTCOF"
        str2=String.format("\r\nGetPrijEvents: %02X (%s)",status,str);

    }

    private fun GetRelStatus(dbuf: ByteArray) {
        var res = "\r\nGetRelStatus: "

        for (i in 0..4) {
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


                datmon.dispR_ProgEn[i] = if (status.hasFlag(Const.Data.REL_PROG_UNLOCK)) getString(R.string.yes) else getString(R.string.no)
                if (m_PriMod == false) {

                    if (!status.hasFlag(Const.Data.REL_LEARN_EN))   datmon.dispR_Learn[i]=getString(R.string.IDS_LRN_DIS)
                    else if (!status.hasFlag(Const.Data.REL_LEARN_INTR))   datmon.dispR_Learn[i]=getString(R.string.IDS_LRN_OK)
                    else datmon.dispR_Learn[i]=getString(R.string.IDS_LRN_INTR)

                    if (status.hasFlag(Const.Data.REL_EM_STATE)) datmon.dispR_ProgEn[i]=getString(R.string.HOZ)

                    datmon.dispR_TransmitFail[i] = if (status.hasFlag(Const.Data.REL_TA_STATE)) getString(R.string.yes) else getString(R.string.no)
                    datmon.dispR_LoopEn[i] = if (status.hasFlag(Const.Data.REL_TIMPR_UNLOCK)) getString(R.string.yes) else getString(R.string.no) //TODO wiper ili loop
                }
            }        //end akt rel;

        }


    }

    private fun GetRelEvents(dbuf: ByteArray) {
        //CString str=_T("");str.Format(_T("\r\nGetRelEvents:%02x"),*pdbuf++);

        var res ="\r\nGetRelEvents:"
        dbuf[globalIndex++]// TODO ??
        for (rel in 0..3)
        {
            val revents = dbuf[globalIndex++].toInt()
            var tstr =""
            if (revents.hasFlag(Const.Data.REL_EV_TL_ON))tstr += "|ON"
            if (revents.hasFlag(Const.Data.REL_EV_TL_OFF)) tstr += "|OFF"
            if (revents.hasFlag(Const.Data.REL_EV_TL_LDIS)) tstr += "|LRNDIS"
            if (revents.hasFlag(Const.Data.REL_EV_TL_LINTR)) tstr += "|LRNINTR"
            if (revents.hasFlag(Const.Data.REL_EV_TL_PROEN) )tstr += "|PROEN"
            if (revents.hasFlag(Const.Data.REL_EV_TL_PRODI)) tstr += "|PRODI"
            res += String.format(" R%d (%02X%s)",rel+1,revents,tstr)
        }

    }

    private fun UpBrTstF(dbuf: ByteArray) {
        if(m_SWVerPri>=90)
            datmon.disp_outage=String.format(" %d",setOprelI(dbuf))
        else
            datmon.disp_outage=String.format(" %d",dbuf[globalIndex++])

        if(m_dbufLen>=6)
        {
            val tin = setOprel4I(dbuf)
            val days=  tin / (3600 * 24)
            val hh:Int= tin % (3600 * 24) / 3600
            val mm:Int = tin % 3600 / 60
            val ss:Int = tin % 60
            datmon.disp_timeInOp= String.format("%d day %02d:%02d:%02d ", days, hh, mm, ss)


        }
    }

    private var m_SWVerPri = 0
    private var m_HWVerPri = 0
    private fun UpUtf(dbuf: ByteArray) {
        var utf=0.0
        if(m_SWVerPri>=90) {
            utf = setOprel4I(dbuf).toDouble() * 1.001
            if (m_KVUt > 0) utf = utf / m_KVUt
            else utf = utf / 0xA20
        }else
            utf=setOprelI(dbuf).toDouble()*0.0028

       datmon.disp_UTF=if(m_PriMod == false) String.format("%0.2f",utf) else ""
    }

    var m_KVUt=0
    private fun UpKVUf(dbuf: ByteArray) {
        m_KVUt=setOprelI(dbuf)
    }

    private fun GetImpName(nr:Int):String {
        var imp=""
        String.format("%d",nr)
        when(nr){
            in 0..4 ->imp=String.format("A%d",nr)
            in 5..12 ->imp=String.format("B%d",nr-4)
            in 13..44->{
                if(nr%2==1)imp=String.format("DP%dz",(nr-13)/2+1)
                else imp=String.format("DP%dv",(nr-13)/2+1)
            }
            else ->imp=String.format("C%d",nr-44)

        }
        return imp
    }


    private fun GetTlgImp(dbuf: ByteArray):String {
        var res=""
        var cnt=0
        val imps = mutableListOf<String>()
        for(i in 0..7){
            for(j in 0..7){
                if(dbuf[i].toInt() and Const.Data.bVtmask[j].toInt() !=0){
                    val imps = mutableListOf<String>()
                }
            }

        }
        val tlg=imps.joinToString(separator = ", ")
        return tlg
    }

    private fun UpZadTeleg(dbuf: ByteArray) {

        var str=GetTlgImp(dbuf);

        //std::vector<CString> names = DBQ::GetTlgNamesMatchingRectlg(tlgx);
        //for (auto value : names) str += " - " + value;

        if (m_PriMod == false) str; // ne mjenjaj
        else str = "" // brisi

        datmon.disp_lastTlg=str

    }


    var m_BrPrekR1=0
    var m_BrPrekR2=0
    var m_BrPrekR3=0
    var m_BrPrekR4=0


    private fun UpEEBroPre(dbuf: ByteArray) {
        m_BrPrekR1=setOprelI(dbuf)
        m_BrPrekR2=setOprelI(dbuf)
        m_BrPrekR3=setOprelI(dbuf)
        m_BrPrekR4=setOprelI(dbuf)
        if((m_AktRel and 0x80) !=0) datmon.dispR1_BrPrek=String.format("%d",m_BrPrekR1)
        if((m_AktRel and 0x40) !=0) datmon.dispR2_BrPrek=String.format("%d",m_BrPrekR2)
        if((m_AktRel and 0x20) !=0) datmon.dispR3_BrPrek=String.format("%d",m_BrPrekR3)
        if((m_AktRel and 0x80) !=0) datmon.dispR4_BrPrek=String.format("%d",m_BrPrekR4)

    }

    var m_AktRel=0
    var m_PriMod=false

    private fun UpAktRel(dbuf: ByteArray) {
        var b=dbuf[globalIndex++].toInt()
        m_AktRel=b and 0xE0
        m_PriMod=if(b and 0x02 !=0) true else false
        dbuf[globalIndex++]

    }

    private fun UpProgFile(dbuf: ByteArray) {
        var paramfile=""
        for(i in 1 until 18)
        {
            val c=dbuf[globalIndex++]
            if(c==0.toByte()) break
            paramfile+=c

        }
        datmon.disp_paramfile=paramfile
        Log.i(Const.Logging.MONITOR,paramfile)
    }


    private fun UpWTimPri(dbuf: ByteArray) {
        var res=""

        var wtime=setOprel3I(dbuf)

        val sec = wtime % 60
        wtime /= 60
        val min = wtime % 60
        wtime /= 60
        val hour = wtime % 24
        wtime /= 24

        res= String.format("%s-%02d:%02d:%02d", context.resources.getStringArray(R.array.a_days)[wtime], hour, min, sec)
        datmon.disp_time=res
        Log.i(Const.Logging.MONITOR,res)

    }

    private fun UpTimPri(dbuf: ByteArray) {
        var res=""

        val timeDate = TimeDate(dbuf[0], dbuf[1], dbuf[2], dbuf[3], dbuf[4], dbuf[5], dbuf[6])
        timeDate.dan = timeDate.dan and 0x0F

        var isInvalid = hasErrors(timeDate)

        timeDate.sek = timeDate.sek and 0x7F

        if (timeDate.dan in 1..7) timeDate.dan =
            (timeDate.dan.toInt() - 1).toByte() else isInvalid = true
        var time=""
        var date=""

        if (isInvalid) {
             time="? : ? : ?"
             date="? - ? - ? - ?"
            res=date+time
            datmon.disp_time=time
            datmon.disp_date=date
            //return res
        }else {
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
        datmon.disp_time=time
        datmon.disp_date=date
       Log.i(Const.Logging.MONITOR,res)
        //return Pair(time, date)



    }

    private fun GetProg7DRx(dbuf: ByteArray, prog: Int, rel: Int) {
        TODO("Not yet implemented")
    }

    fun UpRamEventLog(nr:Int,dbuf: ByteArray) {
        val cnt=nr*4
        if(cnt>= DataStructMon.LOG_EVENT_MAX) return

        for(i in 0..3)
        {

        datmon.m_EvLog

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
}