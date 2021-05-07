package com.mtkreader.views.fragments

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BaseBluetoothFragment
import com.mtkreader.contracts.MonitorContract
import com.mtkreader.presenters.MonitorPresenter
import com.mtkreader.views.dialogs.ConnectingDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_monitor.*

class MonitorView : BaseBluetoothFragment<MonitorContract.Presenter>(), MonitorContract.View {

    private lateinit var connectedDevice: BluetoothDevice
    private lateinit var connectingDialog: Dialog

    override fun onAttach(context: Context) {
        super.onAttach(context)
        unpackExtras()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_monitor, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializePresenter()
        initializeViews()
    }

    private fun unpackExtras() {
        connectedDevice = arguments?.getParcelable(Const.Extras.DEVICE_EXTRA)!!
    }

    private fun initializePresenter() {
        presenter = MonitorPresenter(this)
    }

    private fun initializeViews() {
        requireActivity().title = getString(R.string.monitor)
        requireActivity().toolbar.setNavigationIcon(R.drawable.ic_back_white)

        btn_retry.setOnClickListener { startConnecting() }
        btn_readout.setOnClickListener {
            disableButtons()
            tv_monitor_data.text = ""
            startConnecting()
        }

        btn_event_log.setOnClickListener {
            disableButtons()
            presenter.readEventLog()
        }

        btn_learn.setOnClickListener {
            disableButtons()
            presenter.readLearn()
        }
    }

    private fun startConnecting() {
        presenter.connectToDevice(connectedDevice)
        connectingDialog = ConnectingDialog(requireContext())
        connectingDialog.show()
    }

    override fun onSocketConnected() {
        connectingDialog.dismiss()
        presenter.startCommunication()
        loading_layout.visibility = View.VISIBLE
    }

    override fun displayWaitMessage() {
        //tv_data_read.append(getString(R.string.wait))
    }

    override fun onByte(byte: Byte) {
        loading_layout.visibility = View.GONE
        tv_monitor_data.append(byte.toChar().toString())
    }

    override fun displayStatus(status: String) {
        loading_layout.visibility = View.GONE
        tv_monitor_status.text = status
    }

    override fun onStatusReadingInProgress() {
        enableButtons()
    }

    override fun displayEventLog(eventLog: String) {
        tv_monitor_status.text = eventLog
        enableButtons()
    }

    override fun displayLearn(learn: String) {
        tv_monitor_status.text = learn
        enableButtons()
    }


    private fun disableButtons() {
        btn_readout.isEnabled = false
        btn_event_log.isEnabled = false
        btn_learn.isEnabled = false
    }

    private fun enableButtons() {
        btn_readout.isEnabled = true
        btn_event_log.isEnabled = true
        btn_learn.isEnabled = true
    }


    override fun onError(throwable: Throwable) {
        disableButtons()
        btn_readout.isEnabled = true
        connectingDialog.dismiss()
        loading_layout.visibility = View.GONE
        handleError(throwable) { startConnecting() }
        presenter.stopTimeout()
        presenter.tryReset()
    }
}
