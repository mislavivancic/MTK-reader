package com.mtkreader.views.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothDevice
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
import com.mtkreader.utils.PermissionUtils
import com.mtkreader.views.adapters.ConnectedDevicesRecyclerView
import com.mtkreader.views.adapters.DeviceOperation
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_connect.*

class ConnectView : BaseMVPFragment<ConnectionContract.Presenter>(), ConnectionContract.View,
    ConnectedDevicesRecyclerView.OnItemClickListener {

    companion object {
        private const val TAG = "CONNECT_FRAGMENT"
    }


    private val bluetoothDevices = mutableListOf<BluetoothDevice>()
    private lateinit var connectedDevicesAdapter: ConnectedDevicesRecyclerView
    private lateinit var connectingDialog: Dialog

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
    }

    private fun initializePresenter() {
        presenter = ConnectionPresenter(this)
    }

    private fun initializeViews() {
        requireActivity().title = getString(R.string.paired_devices)
        requireActivity().toolbar.navigationIcon = null
        if (PermissionUtils.hasCoarseLocation(this))
            presenter.initBluetooth()
        else
            PermissionUtils.requestCoarseLocationPermission(this)

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
            connectedDevicesAdapter = ConnectedDevicesRecyclerView(layoutInflater)
            connectedDevicesAdapter.addData(devices)
            connectedDevicesAdapter.setOnClickListener(this)
            rv_devices.apply {
                setHasFixedSize(true)
                adapter = AlphaInAnimationAdapter(connectedDevicesAdapter)
            }
        }
    }

    override fun onClick(device: BluetoothDevice, deviceOperation: DeviceOperation) {
        val deviceBundle = Bundle().apply {
            putParcelable(Const.Extras.DEVICE_EXTRA, device)
        }
        if (deviceOperation == DeviceOperation.TIME_READ || deviceOperation == DeviceOperation.TIME_SET) {
            deviceBundle.apply {
                putSerializable(Const.Extras.DEVICE_OPERATION, deviceOperation)
            }
            findNavController().navigate(R.id.navigateToDisplayTimeView, deviceBundle)
        } else if (deviceOperation == DeviceOperation.PARAM_READ)
            findNavController().navigate(R.id.navigateToReadingView, deviceBundle)
        else if (deviceOperation == DeviceOperation.PARAM_WRITE) {
            findNavController().navigate(R.id.navigateToParamsWriteView, deviceBundle)
        }
    }


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
                    Const.Error.BT_REQUIRED
                ) { requireActivity().finish() }.show()

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
                ErrorDialog(requireContext()) {}.show()
            } else {
                presenter.initBluetooth()
            }
        }
    }

    override fun provideFragment(): Fragment = this


}
