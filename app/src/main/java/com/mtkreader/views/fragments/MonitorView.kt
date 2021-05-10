package com.mtkreader.views.fragments

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BaseBluetoothFragment
import com.mtkreader.contracts.MonitorContract
import com.mtkreader.data.MonitorStatus
import com.mtkreader.presenters.MonitorPresenter
import com.mtkreader.views.adapters.MonitorDataAdapter
import com.mtkreader.views.dialogs.ConnectingDialog
import com.mtkreader.views.fragments.monitor.MonitorEventLogFragment
import com.mtkreader.views.fragments.monitor.MonitorLearnCycleFragment
import com.mtkreader.views.fragments.monitor.MonitorStatusFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_monitor.*

class MonitorView : BaseBluetoothFragment<MonitorContract.Presenter>(), MonitorContract.View {

    private lateinit var connectedDevice: BluetoothDevice
    private lateinit var connectingDialog: Dialog
    private lateinit var monitorDataAdapter: MonitorDataAdapter

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
        monitorDataAdapter = MonitorDataAdapter(this)

        btn_retry.setOnClickListener { startConnecting() }
        btn_readout.setOnClickListener {
            disableButtons()
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
        view_pager.adapter = monitorDataAdapter
        TabLayoutMediator(tab_layout, view_pager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.status)
                1 -> tab.text = getString(R.string.event_log)
                2 -> tab.text = getString(R.string.learn_cycle)
            }
        }.attach()

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
    }

    override fun displayStatus(status: MonitorStatus) {
        loading_layout.visibility = View.GONE
        (monitorDataAdapter.fragments[0] as MonitorStatusFragment).updateMonitor(status)
    }

    override fun onStatusReadingInProgress() {
        enableButtons()
    }

    override fun displayEventLog(eventLog: String) {
        enableButtons()
        view_pager.currentItem = 1
        (monitorDataAdapter.fragments[1] as MonitorEventLogFragment).update(eventLog)
    }

    override fun displayLearn(learn: String) {
        enableButtons()
        view_pager.currentItem = 2
        (monitorDataAdapter.fragments[2] as MonitorLearnCycleFragment).update(learn)
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
