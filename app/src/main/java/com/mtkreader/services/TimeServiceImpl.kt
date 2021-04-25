package com.mtkreader.services

import android.content.Context
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.contracts.TimeContract
import com.mtkreader.data.DeviceDate
import com.mtkreader.data.DeviceTime
import com.mtkreader.data.reading.TimeDate
import com.mtkreader.data.writing.DataTXMessage
import com.mtkreader.utils.DataUtils
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

class TimeServiceImpl : TimeContract.Service {

    override fun extractTimeData(context: Context, data: List<Char>, hardwareVersion: Int): Pair<String, String> {
        val dbuf = parseTimeData(data)
        return generateTimeString(context, dbuf, hardwareVersion)
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

    override fun generateTimeWriteMessage(time: DeviceTime, deviceDate: DeviceDate): DataTXMessage {
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
            return createTimeWriteMessage(timeDateString)
        }
    }

    override fun createTimeWriteMessage(time: String):DataTXMessage {
        var j = 0
        val messageSendData = DataTXMessage()
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

        }
        return messageSendData
    }

}