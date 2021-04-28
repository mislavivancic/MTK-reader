package com.mtkreader.views.fragments

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BaseBluetoothFragment
import com.mtkreader.contracts.ReadingContract
import com.mtkreader.presenters.ReadingPresenter
import com.mtkreader.utils.SharedPrefsUtils
import com.mtkreader.views.dialogs.ConnectingDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_reading.*

class ReadingView : BaseBluetoothFragment<ReadingContract.Presenter>(), ReadingContract.View {

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
        return inflater.inflate(R.layout.fragment_reading, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializePresenter()
        initializeViews()
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
        requireActivity().title = getString(R.string.read_parameters)
        requireActivity().toolbar.setNavigationIcon(R.drawable.ic_back_white)

        tv_data_read.movementMethod = ScrollingMovementMethod()
        btn_retry.setOnClickListener { startConnecting() }
        btn_readout.setOnClickListener { startConnecting() }
        btn_display_old.setOnClickListener {
            findNavController().navigate(R.id.navigateToDisplayDataView)
        }
        btn_display_old.isEnabled = SharedPrefsUtils.getReadData(requireContext()) != null
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

    override fun onByte(byte: Byte) {
        loading_layout.visibility = View.GONE
        tv_data_read.append(byte.toChar().toString())
    }

    override fun onReadoutDone(readout: String) {
        val dataBundle = Bundle().apply {
            putString(Const.Extras.DATA_EXTRA, readout)
        }
        SharedPrefsUtils.saveReadData(requireContext(), readout)
        findNavController().navigate(R.id.navigateToDisplayDataView, dataBundle)
    }

    override fun displayWaitMessage() {
        tv_data_read.append(getString(R.string.wait))
    }

    override fun onError(throwable: Throwable) {
        connectingDialog.dismiss()
        loading_layout.visibility = View.GONE
        handleError(throwable) { startConnecting() }
        presenter.stopTimeout()
        presenter.tryReset()
    }
}
