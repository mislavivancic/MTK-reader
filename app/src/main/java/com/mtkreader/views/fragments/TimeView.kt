package com.mtkreader.views.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import com.github.ivbaranov.rxbluetooth.exceptions.ConnectionClosedException
import com.ikovac.timepickerwithseconds.MyTimePickerDialog
import com.ikovac.timepickerwithseconds.TimePicker
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.Const.Data.ETX
import com.mtkreader.commons.base.BaseMVPFragment
import com.mtkreader.contracts.TimeContract
import com.mtkreader.data.DeviceDate
import com.mtkreader.data.DeviceTime
import com.mtkreader.presenters.TimePresenter
import com.mtkreader.utils.CommunicationUtil
import com.mtkreader.utils.DataUtils.getHardwareVersion
import com.mtkreader.utils.TimeUtils
import com.mtkreader.views.adapters.DeviceOperation
import com.mtkreader.views.dialogs.ConnectingDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_time.*
import net.alexandroid.utils.mylogkt.logI
import java.io.IOException

class TimeView : BaseMVPFragment<TimeContract.Presenter>(), TimeContract.View,
    MyTimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    companion object {
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
    private var isConnectedToDevice = false
    private var isReadingData = false
    private var isTimeWriteFinished = false
    private var hardwareVersion = 0
    private lateinit var time: DeviceTime
    private lateinit var deviceDate: DeviceDate
    private lateinit var deviceOperation: DeviceOperation

    override fun onAttach(context: Context) {
        super.onAttach(context)
        unpackExtras()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_time, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializePresenter()
        initializeViews()
        if (deviceOperation == DeviceOperation.TIME_READ)
            startReading()
    }

    private fun unpackExtras() {
        val deviceArgument =
            requireArguments().getParcelable<BluetoothDevice>(Const.Extras.DEVICE_EXTRA)
        if (deviceArgument != null)
            connectedDevice = deviceArgument
        else
            requireActivity().finish()
        deviceOperation =
            requireArguments().getSerializable(Const.Extras.DEVICE_OPERATION) as DeviceOperation?
                ?: DeviceOperation.TIME_READ
    }

    private fun initializePresenter() {
        presenter = TimePresenter(this)
    }

    private fun initializeViews() {
        requireActivity().title = getString(R.string.program_time)
        requireActivity().toolbar.setNavigationIcon(R.drawable.ic_back_white)

        tv_data_read.movementMethod = ScrollingMovementMethod()
        btn_time_pick.setOnClickListener {
            TimeUtils.provideTimePicker(requireContext(), this).show()
        }
        btn_date_pick.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                this,
                date_picker.year,
                date_picker.month,
                date_picker.dayOfMonth
            ).show()
        }

        btn_program_time.setOnClickListener {
            startReading()
        }
        btn_retry.setOnClickListener {
            startReading()
            toast(getString(R.string.retrying))
        }
        if (deviceOperation == DeviceOperation.TIME_READ) {
            btn_retry.text = getString(R.string.retry_time_read)
        } else if (deviceOperation == DeviceOperation.TIME_SET) {
            btn_retry.text = getString(R.string.retry_time_set)
            pick_date_time_container.visibility = View.VISIBLE
            btn_program_time.visibility = View.VISIBLE
        }
        deviceDate = DeviceDate(date_picker.year, date_picker.month, date_picker.dayOfMonth)
        tv_date.text =
            String.format(
                getString(R.string.date_time_format_d),
                deviceDate.day,
                deviceDate.month,
                deviceDate.year
            )
        checkTimeDateSet()
    }

    private fun startReading() {
        presenter.connectToDevice(connectedDevice)
        connectingDialog = ConnectingDialog(requireContext())
        connectingDialog.show()
    }

    override fun onSocketConnected(socket: BluetoothSocket) {
        this.socket = socket

        connectingDialog.dismiss()
        loading_layout.visibility = View.VISIBLE
        presenter.readStream(this.socket)
        btn_retry.visibility = View.GONE
        presenter.initDeviceCommunication()
    }

    override fun onReceiveBytes(byte: Byte) {
        loading_layout.visibility = View.GONE
        presenter.stopTimeout()
        data.add(byte.toChar())
        readingData.add(byte.toChar())
        tv_data_read.append(byte.toChar().toString())
        logI("${byte.toChar()} -> $byte ", customTag = Const.Logging.RECEIVED)
        if (data.contains(FIRST_LINE_TOKEN_FIRST) && data.contains(FIRST_LINE_TOKEN_SECOND) && !isReadingData) {
            hardwareVersion = getHardwareVersion(data.joinToString("").toByteArray())
            data.clear()
            CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.SECOND_INIT)
            isConnectedToDevice = true
        }
        if (deviceOperation == DeviceOperation.TIME_READ)
            handleTimeReading()
        else if (deviceOperation == DeviceOperation.TIME_SET && this::time.isInitialized && this::deviceDate.isInitialized)
            initTimeWrite()
    }

    private fun handleTimeReading() {
        if ((data.contains(SECOND_LINE_TOKEN) || data.contains(SECOND_LINE_TOKEN_OTHER)) && !isReadingData) {
            data.clear()
            presenter.getTime()
            isReadingData = true
        }
        if (data.contains(Const.Tokens.GET_TIME_END_TOKEN)) {
            val timeData = mutableListOf<Char>()
            timeData.addAll(data.filter { it != ETX.toChar() && it != 10.toChar() })
            data.clear()
            presenter.extractTimeData(requireContext(), timeData, hardwareVersion)
        }
    }

    private fun initTimeWrite() {
        if ((data.contains(SECOND_LINE_TOKEN) || data.contains(SECOND_LINE_TOKEN_OTHER)) && !isReadingData) {
            data.clear()
            isReadingData = true
            presenter.setTimeDate(time, deviceDate)
        }
        if (isReadingData && data.isNotEmpty() && !isTimeWriteFinished) {
            presenter.setReadData(data)
            data.clear()
        }
        if (data.contains(Const.Tokens.GET_TIME_END_TOKEN) && isTimeWriteFinished) {
            val timeData = mutableListOf<Char>()
            timeData.addAll(data.filter { it != ETX.toChar() && it != 10.toChar() })
            data.clear()
            presenter.extractTimeData(requireContext(), timeData, hardwareVersion)
        }

    }

    override fun onTimeWriteResult(isSuccessful: Boolean) {
        if (isSuccessful) {
            btn_retry.visibility = View.VISIBLE
            toast(getString(R.string.setting_time_is_impossible))
        } else toast(getString(R.string.time_changed))

        data.clear()
        presenter.getTime()
        isTimeWriteFinished = true
    }

    override fun displayTimeData(timeDate: Pair<String, String>) {
        device_time_container.visibility = View.VISIBLE
        tv_current_time.text = String.format("%s", timeDate.first)
        tv_current_date.text = String.format("%s", timeDate.second)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int, seconds: Int) {
        time = DeviceTime(hourOfDay, minute, seconds)
        tv_time.text =
            String.format(getString(R.string.day_time_format_d), hourOfDay, minute, seconds)
        program_time_container.visibility = View.VISIBLE
        checkTimeDateSet()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        deviceDate = DeviceDate(year, month, dayOfMonth)
        tv_date.text =
            String.format(getString(R.string.date_time_format_d), dayOfMonth, month, year)
        program_time_container.visibility = View.VISIBLE
        checkTimeDateSet()
    }

    private fun checkTimeDateSet() {
        btn_program_time.isEnabled = this::time.isInitialized && this::deviceDate.isInitialized
    }

    override fun displayWaitMessage() {
        tv_data_read.append(getString(R.string.wait))
    }

    override fun onError(throwable: Throwable) {
        connectingDialog.dismiss()
        loading_layout.visibility = View.GONE
        when (throwable) {
            is ConnectionClosedException -> {
                btn_retry.visibility = View.VISIBLE
            }
            is IOException -> {
                toast(getString(R.string.set_probe_in_connecting))
                btn_retry.visibility = View.VISIBLE
            }
            else -> displayErrorPopup(throwable)
        }
        presenter.stopTimeout()

    }

    override fun onDestroy() {
        presenter.closeConnection()
        super.onDestroy()
    }
}
