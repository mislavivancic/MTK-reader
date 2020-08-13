package com.mtkreader.views.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BaseMVPFragment
import com.mtkreader.commons.base.ErrorDialog
import com.mtkreader.contracts.ConnectionContract
import com.mtkreader.presenters.ConnectionPresenter
import com.mtkreader.utils.CommunicationUtil
import com.mtkreader.utils.PermissionUtils
import com.mtkreader.views.adapters.ConnectedDevicesRecyclerView
import com.mtkreader.views.dialogs.CantFindDeviceDialog
import com.mtkreader.views.dialogs.ConnectingDialog
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import kotlinx.android.synthetic.main.fragment_connect.*

class ConnectFragment : BaseMVPFragment<ConnectionContract.Presenter>(), ConnectionContract.View,
    ConnectedDevicesRecyclerView.OnItemClickListener {

    companion object {
        private const val TAG = "CONNECT_FRAGMENT"
    }


    private val bluetoothDevices = mutableListOf<BluetoothDevice>()
    private lateinit var connectedDevicesAdapter: ConnectedDevicesRecyclerView
    private lateinit var connectingDialog: Dialog
    private val data = mutableListOf<Char>()
    private lateinit var socket: BluetoothSocket
    private var isReadingData = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_connect, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializePresenter()
        initializeViews()
        initializeRoutes()
    }

    private fun initializePresenter() {
        presenter = ConnectionPresenter(this)
    }

    private fun initializeViews() {
        if (PermissionUtils.hasCoarseLocation(this))
            presenter.initBluetooth()
        else
            PermissionUtils.requestCoarseLocationPermission(this)

    }

    private fun initializeRoutes() {
        tv_fragment_title.setOnClickListener {
            findNavController().navigate(R.id.navigateToReadingFragment)
        }
    }

    override fun onBluetoothInit() {
        presenter.getConnectedDevices()
        presenter.observeDevices()
    }


    override fun onObservedDevice(device: BluetoothDevice) {
        bluetoothDevices.add(device)
    }

    override fun onConnectedDevices(devices: Set<BluetoothDevice>?) {
        if (devices != null) {
            connectedDevicesAdapter = ConnectedDevicesRecyclerView(requireContext(), layoutInflater)
            connectedDevicesAdapter.addData(devices)
            connectedDevicesAdapter.setOnClickListener(this)
            rv_devices.apply {
                adapter = AlphaInAnimationAdapter(connectedDevicesAdapter)
            }

            val device = devices.find { it.name.toUpperCase().contains(Const.DeviceConstants.NAME) }

            if (device == null)
                CantFindDeviceDialog(requireContext()).show()
        }
    }

    override fun onClick(device: BluetoothDevice) {
        presenter.connectToDevice(device)
        connectingDialog = ConnectingDialog(requireContext())
        connectingDialog.show()
    }

    override fun onSocketConnected(socket: BluetoothSocket) {
        this.socket = socket
        rv_devices.visibility = View.GONE
        tv_data_read.visibility = View.VISIBLE

        connectingDialog.dismiss()
        presenter.readStream(this.socket)

        CommunicationUtil.writeToSocket(this.socket, Const.DeviceConstants.FIRST_INIT)
    }

    // TODO how to sync
    override fun onReceiveBytes(byte: Byte) {
        data.add(byte.toChar())
        tv_data_read.append(byte.toChar().toString())
        println("$byte -> ${byte.toChar()}")
        if (data.contains(13.toByte().toChar()) && data.contains(10.toByte().toChar())) {
            data.clear()
            CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.SECOND_INIT)
        }
        if (data.contains(6.toByte().toChar())) {
            data.clear()
            CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.ACK)
            isReadingData = true
        }
    }

    /*
        override fun onSocketConnected(socket: BluetoothSocket) {
        rv_devices.visibility = View.GONE
        tv_data_read.visibility = View.VISIBLE

        connectingDialog.dismiss()
        presenter.readStream(socket)

        CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.FIRST_INIT)
        Thread.sleep(5000)
        CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.SECOND_INIT)
        Thread.sleep(2000)
        CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.ACK)
    }

    @SuppressLint("SetTextI18n")
    override fun onReceiveBytes(byte: Byte) {
        data.add(byte.toChar())
        tv_data_read.append(byte.toChar().toString())
    }
     */

    override fun onError(throwable: Throwable) {
        connectingDialog.dismiss()
        displayErrorPopup(throwable)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == Const.RequestCode.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                presenter.getConnectedDevices()
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                ErrorDialog(
                    requireContext(),
                    Const.Error.BT_REQUIRED,
                    View.OnClickListener { requireActivity().finish() }).show()

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PermissionUtils.COARSE_LOCATION_REQUEST_CODE) {
            if (!permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION) || grantResults[0] == Const.PermissionCode.DENIED) {
                ErrorDialog(requireContext()).show()
            } else {
                presenter.initBluetooth()
            }
        }
    }

    override fun provideFragment(): Fragment = this


}