package com.mtkreader.views.fragments

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ikovac.timepickerwithseconds.MyTimePickerDialog
import com.ikovac.timepickerwithseconds.TimePicker
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.Const.Data.ACK
import com.mtkreader.commons.Const.Data.COMPLETE
import com.mtkreader.commons.Const.Data.EOT
import com.mtkreader.commons.Const.Data.ETX
import com.mtkreader.commons.Const.Data.NAK
import com.mtkreader.commons.Const.Data.SOH
import com.mtkreader.commons.Const.Data.STX
import com.mtkreader.commons.base.BaseMVPFragment
import com.mtkreader.contracts.ReadingContract
import com.mtkreader.data.DeviceDate
import com.mtkreader.data.DeviceTime
import com.mtkreader.data.reading.TimeDate
import com.mtkreader.data.writing.DataRXMessage
import com.mtkreader.data.writing.DataTXTMessage
import com.mtkreader.presenters.ReadingPresenter
import com.mtkreader.utils.CommunicationUtil
import com.mtkreader.utils.DataUtils.getHardwareVersion
import com.mtkreader.utils.SharedPrefsUtils
import com.mtkreader.utils.TimeUtils
import com.mtkreader.views.dialogs.ConnectingDialog
import kotlinx.android.synthetic.main.fragment_reading.*
import java.util.*
import kotlin.concurrent.thread
import kotlin.experimental.or
import kotlin.experimental.xor

class ReadingView : BaseMVPFragment<ReadingContract.Presenter>(), ReadingContract.View,
    MyTimePickerDialog.OnTimeSetListener {

    companion object {
        private const val TAG = "READING_FRAGMENT"

        private const val FIRST_LINE_TOKEN_FIRST = 13.toByte().toChar()
        private const val FIRST_LINE_TOKEN_SECOND = 10.toByte().toChar()
        private const val SECOND_LINE_TOKEN = 6.toByte().toChar()
        private const val SECOND_LINE_TOKEN_OTHER = 127.toByte().toChar()
    }

    private lateinit var connectedDevice: BluetoothDevice
    private lateinit var connectingDialog: Dialog
    private val data = mutableListOf<Char>()
    private val readingData = mutableListOf<Char>()
    private lateinit var socket: BluetoothSocket
    private var isReadingData = false
    private var hardwareVersion = 0
    private lateinit var time: DeviceTime
    private lateinit var deviceDate: DeviceDate
    private var readMessageData = DataRXMessage()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        unpackExtras()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reading, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializePresenter()
        initializeViews()
        //startReading()
    }

    private fun unpackExtras() {
        val deviceArgument = arguments?.getParcelable<BluetoothDevice>(Const.Extras.DEVICE_EXTRA)
        if (deviceArgument != null)
            connectedDevice = deviceArgument
        else
            requireActivity().finish()
    }

    private fun initializePresenter() {
        presenter = ReadingPresenter(this)
    }

    private fun initializeViews() {
        tv_data_read.movementMethod = ScrollingMovementMethod()
        btn_time_pick.setOnClickListener {
            TimeUtils.provideTimePicker(requireContext(), this).show()
        }
    }

    private fun startReading() {
        presenter.connectToDevice(connectedDevice)
        connectingDialog = ConnectingDialog(requireContext())
        connectingDialog.show()
    }

    override fun onSocketConnected(socket: BluetoothSocket) {
        this.socket = socket

        connectingDialog.dismiss()
        presenter.readStream(this.socket)

        CommunicationUtil.writeToSocket(this.socket, Const.DeviceConstants.FIRST_INIT)
    }

    override fun onReceiveBytes(byte: Byte) {
        data.add(byte.toChar())
        readingData.add(byte.toChar())
        tv_data_read.append(byte.toChar().toString())
        println("MESSAGES Read ${byte.toChar()} -> $byte ")
        //handleTimeReading()
        initTimeWrite()
        //handleParameterReading()
    }

    private fun handleParameterReading() {
        if (data.contains(FIRST_LINE_TOKEN_FIRST) && data.contains(FIRST_LINE_TOKEN_SECOND) && !isReadingData) {
            data.clear()
            CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.SECOND_INIT)
        }
        if ((data.contains(SECOND_LINE_TOKEN) || data.contains(SECOND_LINE_TOKEN_OTHER)) && !isReadingData) {
            data.clear()
            CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.ACK)
            isReadingData = true
        }
        if (data.contains(Const.Tokens.PARAM_READ_END_TOKEN)) {
            CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.RESET)
            socket.close()
            presenter.closeConnection()

            val dataBundle = Bundle().apply {

                putString(Const.Extras.DATA_EXTRA, readingData.joinToString(""))
            }
            SharedPrefsUtils.saveReadData(requireContext(), readingData.joinToString(""))
            data.clear()
            findNavController().navigate(R.id.navigateToDisplayDataView, dataBundle)
        }
    }

    private fun handleTimeReading() {
        if (data.contains(FIRST_LINE_TOKEN_FIRST) && data.contains(FIRST_LINE_TOKEN_SECOND) && !isReadingData) {
            hardwareVersion = getHardwareVersion(data.joinToString("").toByteArray())
            data.clear()
            CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.SECOND_INIT)
        }

        if ((data.contains(SECOND_LINE_TOKEN) || data.contains(SECOND_LINE_TOKEN_OTHER)) && !isReadingData) {
            data.clear()
            CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.GET_TIME)
            isReadingData = true
        }
        if (data.contains(Const.Tokens.GET_TIME_END_TOKEN)) {
            CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.RESET)
            presenter.extractTimeData(requireContext(), data, hardwareVersion)

            socket.close()
            presenter.closeConnection()

            val dataBundle = Bundle().apply {

                putString(Const.Extras.DATA_EXTRA, data.joinToString(""))
            }
            data.clear()
            //findNavController().navigate(R.id.navigateToDisplayTimeView, dataBundle)
        }
    }

    private fun initTimeWrite() {
        if (data.contains(FIRST_LINE_TOKEN_FIRST) && data.contains(FIRST_LINE_TOKEN_SECOND) && !isReadingData) {
            hardwareVersion = getHardwareVersion(data.joinToString("").toByteArray())
            data.clear()
            CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.SECOND_INIT)
        }
        if ((data.contains(SECOND_LINE_TOKEN) || data.contains(SECOND_LINE_TOKEN_OTHER)) && !isReadingData) {
            data.clear()
            isReadingData = true
            thread {
                setDeviceTime(time, deviceDate)
            }
        }

    }

    override fun displayTimeData(time: String) {
        tv_current_time.text = String.format(getString(R.string.device_time_s), time)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int, seconds: Int) {
        time = DeviceTime(hourOfDay, minute, seconds)
        deviceDate = DeviceDate(date_picker.year, date_picker.month, date_picker.dayOfMonth)
        startReading()
    }

    private fun setDeviceTime(time: DeviceTime, deviceDate: DeviceDate) {
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
            writeTime(timeDate)
        }
    }

    private fun writeTime(timeDate: TimeDate) {
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

        if (!waitAnswer(timeString)) println("Set in receiver mode!")
        else println("Time set!")
    }

    private fun waitAnswer(time: String): Boolean {
        var isSuccessful = false
        loop@ for (i in 1..3) {
            sendStringToDevice(time)
            readMessageData = DataRXMessage()
            if (waitMessage()) {
                when (readMessageData.status) {
                    ACK -> {
                        isSuccessful = true
                        break@loop
                    }
                    NAK -> continue@loop
                    COMPLETE -> break@loop

                }
            }

        }
        readMessageData = DataRXMessage()
        return isSuccessful
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
                    if (readMessageData.status == ETX || readMessageData.status == EOT) {
                        readMessageData.bcc = readMessageData.bcc xor dataByte.toByte()
                        if (readMessageData.bcc == 0.toByte()) {
                            readMessageData.status = COMPLETE
                        } else {
                            readMessageData.proterr = 0xCC.toByte()
                        }
                        return true
                    } else {
                        if (readMessageData.status == SOH || readMessageData.status == STX)
                            readMessageData.bcc = readMessageData.bcc xor dataByte.toByte()

                        when (dataByte.toByte()) {
                            SOH -> {
                                if (readMessageData.count == 0)
                                    readMessageData.status = SOH
                                else
                                    readMessageData.proterr = SOH
                                readMessageData.buffer[readMessageData.count++] = dataByte.toByte()
                            }
                            STX -> {
                                if (readMessageData.status == SOH || readMessageData.count == 0)
                                    readMessageData.status = STX
                                else
                                    readMessageData.proterr = STX
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
                                    readMessageData.status = COMPLETE
                                    return true
                                }
                            }
                            ETX -> {
                                readMessageData.status = ETX
                            }
                            EOT -> {
                                readMessageData.status = EOT
                            }
                            ACK -> {
                                readMessageData.status = ACK
                                return true
                            }
                            NAK -> {
                                readMessageData.status = NAK
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
            messageSendData.buffer[j++] = SOH
            for (char in time) {
                messageSendData.buffer[j++] = char.toByte()
                messageSendData.bcc = messageSendData.bcc xor char.toByte()
            }
            messageSendData.buffer[j++] = ETX
            messageSendData.bcc = messageSendData.bcc xor ETX
            messageSendData.buffer[j++] = messageSendData.bcc
            messageSendData.count = j

            CommunicationUtil.writeToSocket(
                socket,
                messageSendData.buffer.take(messageSendData.count).toByteArray()
            )
        }
    }

    override fun onError(throwable: Throwable) {
        connectingDialog.dismiss()
        displayErrorPopup(throwable)
    }

    override fun provideFragment(): Fragment = this

}
