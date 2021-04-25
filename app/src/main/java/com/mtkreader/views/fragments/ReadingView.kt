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
import com.mtkreader.commons.base.BaseMVPFragment
import com.mtkreader.contracts.ReadingContract
import com.mtkreader.presenters.ReadingPresenter
import com.mtkreader.utils.SharedPrefsUtils
import com.mtkreader.views.dialogs.ConnectingDialog
import kotlinx.android.synthetic.main.fragment_reading.*

class ReadingView : BaseMVPFragment<ReadingContract.Presenter>(), ReadingContract.View {

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
        startConnecting()
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
    }

    private fun startConnecting() {
        presenter.connectToDevice(connectedDevice)
        connectingDialog = ConnectingDialog(requireContext())
        connectingDialog.show()
    }

    override fun onSocketConnected() {
        connectingDialog.dismiss()
        presenter.startCommunication()
    }

    override fun onByte(byte: Byte) {
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
        displayErrorPopup(throwable)
    }
}
