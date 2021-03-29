package com.mtkreader.presenters

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.github.ivbaranov.rxbluetooth.BluetoothConnection
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BasePresenter
import com.mtkreader.contracts.ReadingContract
import com.mtkreader.data.reading.TimeDate
import com.mtkreader.utils.DataUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or

class ReadingPresenter(private val view: ReadingContract.View) : BasePresenter(view),
    ReadingContract.Presenter, KoinComponent {

    private val bluetoothManager: RxBluetooth by inject()
    private lateinit var connection: BluetoothConnection

    override fun connectToDevice(device: BluetoothDevice) {
        addDisposable(
            bluetoothManager.connectAsClient(device, UUID.fromString(Const.BluetoothConstants.UUID))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::onSocketConnected, view::onError)
        )
    }

    override fun readStream(socket: BluetoothSocket) {
        connection = BluetoothConnection(socket)

        addDisposable(
            connection.observeByteStream()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(view::onReceiveBytes, view::onError)
        )
    }

    override fun closeConnection() {
        connection.closeConnection()
        clear()
    }

    override fun extractTimeData(context: Context, data: List<Char>, hardwareVersion: Int) {
        val dbuf = parseTimeData(data)
        view.displayTimeData(generateTimeString(context, dbuf, hardwareVersion))
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
    ): String {
        val timeDate = TimeDate(dbuf[0], dbuf[1], dbuf[2], dbuf[3], dbuf[4], dbuf[5], dbuf[6])
        timeDate.dan = timeDate.dan and 0x0F

        var isInvalid = hasErrors(timeDate)

        timeDate.sek = timeDate.sek and 0x7F

        if (timeDate.dan in 1..7) timeDate.dan =
            (timeDate.dan.toInt() - 1).toByte() else isInvalid = true

        if (isInvalid) {
            return String.format(
                context.getString(R.string.wrong_value_time),
                timeDate.dan,
                timeDate.sat,
                timeDate.min,
                timeDate.sek,
                timeDate.dat,
                timeDate.mje,
                timeDate.god
            )
        }
        var dateTime = String.format(
            context.getString(R.string.day_time_format),
            context.resources.getStringArray(R.array.a_days)[timeDate.dan.toInt()],
            timeDate.sat,
            timeDate.min,
            timeDate.sek
        )
        if (hardwareVersion == Const.Data.TIP_PA)
            dateTime = String.format(
                context.getString(R.string.date_time_format),
                timeDate.dat,
                timeDate.mje,
                timeDate.god
            )
        return dateTime
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
}