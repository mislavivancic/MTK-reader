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
import com.mtkreader.commons.base.BaseMVPFragment
import com.mtkreader.contracts.ReadingContract
import com.mtkreader.data.DeviceDate
import com.mtkreader.data.DeviceTime
import com.mtkreader.data.writing.DataRXMessage
import com.mtkreader.presenters.ReadingPresenter
import com.mtkreader.utils.CommunicationUtil
import com.mtkreader.utils.DataUtils.getHardwareVersion
import com.mtkreader.utils.SharedPrefsUtils
import com.mtkreader.utils.TimeUtils
import com.mtkreader.views.dialogs.ConnectingDialog
import kotlinx.android.synthetic.main.fragment_reading.*
import net.alexandroid.utils.mylogkt.logD
import net.alexandroid.utils.mylogkt.logI

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
        startReading()
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
        logI("${byte.toChar()} -> $byte ", customTag = Const.Logging.RECEIVED)
        handleTimeReading()
        //initTimeWrite()
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
            presenter.setTimeDate(time, deviceDate)
        }
        if (isReadingData && data.isNotEmpty()) {
            presenter.setData(data)
            data.clear()
        }

    }

    override fun onTimeWriteResult(isSuccessful: Boolean) {
        if (isSuccessful) toast(getString(R.string.setting_time_is_impossible))
        else toast(getString(R.string.time_changed))

    }


    override fun displayTimeData(time: String) {
        tv_current_time.text = String.format(getString(R.string.device_time_s), time)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int, seconds: Int) {
        time = DeviceTime(hourOfDay, minute, seconds)
        deviceDate = DeviceDate(date_picker.year, date_picker.month, date_picker.dayOfMonth)
        startReading()
    }

    override fun onError(throwable: Throwable) {
        connectingDialog.dismiss()
        displayErrorPopup(throwable)
    }

    override fun provideFragment(): Fragment = this

}
