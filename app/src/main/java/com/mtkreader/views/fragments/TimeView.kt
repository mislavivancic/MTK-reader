package com.mtkreader.views.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import com.ikovac.timepickerwithseconds.MyTimePickerDialog
import com.ikovac.timepickerwithseconds.TimePicker
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BaseBluetoothFragment
import com.mtkreader.contracts.TimeContract
import com.mtkreader.data.DeviceDate
import com.mtkreader.data.DeviceTime
import com.mtkreader.presenters.TimePresenter
import com.mtkreader.utils.TimeUtils
import com.mtkreader.views.adapters.DeviceOperation
import com.mtkreader.views.dialogs.ConnectingDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_time.*
import java.util.*


class TimeView : BaseBluetoothFragment<TimeContract.Presenter>(), TimeContract.View,
    MyTimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {


    private lateinit var connectedDevice: BluetoothDevice
    private lateinit var connectingDialog: Dialog
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
        presenter = TimePresenter(this, deviceOperation)
    }

    private fun initializeViews() {

        if (deviceOperation == DeviceOperation.TIME_READ) {
            requireActivity().title = getString(R.string.read_time)
            btn_retry.text = getString(R.string.retry_time_read)
        } else if (deviceOperation == DeviceOperation.TIME_SET) {
            requireActivity().title = getString(R.string.program_time)
            btn_retry.text = getString(R.string.retry_time_set)
            pick_date_time_container.visibility = View.VISIBLE
            btn_program_time.visibility = View.VISIBLE
            program_time_container.visibility = View.VISIBLE
        }

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

        val calendar: Calendar = Calendar.getInstance()
        val hours: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes: Int = calendar.get(Calendar.MINUTE)
        val seconds: Int = calendar.get(Calendar.SECOND)
        time = DeviceTime(hours, minutes, seconds)
        deviceDate = DeviceDate(date_picker.year, date_picker.month, date_picker.dayOfMonth)
        tv_date.text =
            String.format(
                getString(R.string.date_time_format_d),
                deviceDate.day,
                deviceDate.month,
                deviceDate.year
            )
        tv_time.text = String.format(getString(R.string.day_time_format_d), hours, minutes, seconds)
        checkTimeDateSet()
        presenter.setTimeDate(time, deviceDate)
    }

    private fun startReading() {
        tv_error.text = ""
        presenter.connectToDevice(connectedDevice)
        connectingDialog = ConnectingDialog(requireContext())
        connectingDialog.show()
    }

    override fun onSocketConnected() {
        connectingDialog.dismiss()
        loading_layout.visibility = View.VISIBLE
        btn_retry.visibility = View.GONE
        presenter.startCommunication()
    }

    override fun onTimeWriteResult(isSuccessful: Boolean) {
        if (isSuccessful) {
            toast(getString(R.string.time_changed))
        } else {
            snack(getString(R.string.setting_time_is_impossible), actionText = getString(R.string.ok), action = {
                startReading()
                toast(getString(R.string.retrying))
            })
        }
    }

    override fun displayTimeData(timeDate: Pair<String, String>) {
        device_time_container.visibility = View.VISIBLE
        loading_layout.visibility = View.GONE
        tv_current_time.text = String.format("%s", timeDate.first)
        tv_current_date.text = String.format("%s", timeDate.second)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int, seconds: Int) {
        time = DeviceTime(hourOfDay, minute, seconds)
        tv_time.text =
            String.format(getString(R.string.day_time_format_d), hourOfDay, minute, seconds)
        program_time_container.visibility = View.VISIBLE
        presenter.setTimeDate(time, deviceDate)
        checkTimeDateSet()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        deviceDate = DeviceDate(year, month, dayOfMonth)
        tv_date.text =
            String.format(getString(R.string.date_time_format_d), dayOfMonth, month, year)
        program_time_container.visibility = View.VISIBLE
        presenter.setTimeDate(time, deviceDate)
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
        handleError(throwable) { startReading() }
        presenter.stopTimeout()
        presenter.stopTimeFetch()
        presenter.tryReset()
    }

    override fun onDestroy() {
        presenter.closeConnection()
        super.onDestroy()
    }
}
