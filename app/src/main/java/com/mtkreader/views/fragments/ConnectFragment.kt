package com.mtkreader.views.fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.ErrorDialog
import com.mtkreader.utils.PermissionUtils
import kotlinx.android.synthetic.main.fragment_connect.*

class ConnectFragment : Fragment() {

    companion object {
        private const val TAG = "CONNECT_FRAGMENT"
    }

    private lateinit var bluetoothManager: RxBluetooth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_connect, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeViews()
        initializeRoutes()
    }

    private fun initializeViews() {
        PermissionUtils.checkCoarseLocationPermission(this)
        bluetoothManager = RxBluetooth(context)
    }

    private fun initializeRoutes() {
        tv_fragment_title.setOnClickListener {
            findNavController().navigate(R.id.navigateToReadingFragment)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PermissionUtils.COARSE_LOCATION_REQUEST_CODE) {
            if (!permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION) || grantResults[0] == Const.PermissionCode.DENIED) {
                ErrorDialog(context!!).show()
            }
        }
    }

}