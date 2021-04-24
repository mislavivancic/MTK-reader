package com.mtkreader.views.fragments

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BaseMVPFragment
import com.mtkreader.contracts.ParamsWriteContract
import com.mtkreader.presenters.ParamsWritePresenter
import com.mtkreader.trimAndSplit
import com.mtkreader.utils.SharedPrefsUtils
import com.mtkreader.views.dialogs.ConnectingDialog
import kotlinx.android.synthetic.main.fragment_file_pick.*

class ParamsWriteView : BaseMVPFragment<ParamsWriteContract.Presenter>(), ParamsWriteContract.View {

    companion object {
        private const val FILE_PICK_CODE = 1
    }

    private lateinit var connectedDevice: BluetoothDevice
    private lateinit var connectingDialog: Dialog
    private val fileLines = mutableListOf<String>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        unpackExtras()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_file_pick, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initPresenter()
        initViews()
    }

    private fun unpackExtras() {
        val deviceArgument = requireArguments().getParcelable<BluetoothDevice>(Const.Extras.DEVICE_EXTRA)
        if (deviceArgument != null)
            connectedDevice = deviceArgument
        else
            requireActivity().finish()
    }

    private fun initPresenter() {
        presenter = ParamsWritePresenter(this)
    }

    private fun initViews() {
        btn_pick_file.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/octet-stream*"

                addCategory(Intent.CATEGORY_DEFAULT)
            }.also {
                startActivityForResult(it, FILE_PICK_CODE)
            }
        }
        val lastFileData = SharedPrefsUtils.getLastFileRead(requireContext())
        if (lastFileData != null) {
            //presenter.extractFileData(lastFileData.trimAndSplit())
            btn_last_file.visibility = View.VISIBLE
            btn_last_file.setOnClickListener {
                presenter.extractFileData(lastFileData.trimAndSplit())
            }
        }
    }

    override fun onReadyToConnect() {
        presenter.connectToDevice(connectedDevice)
        connectingDialog = ConnectingDialog(requireContext())
        connectingDialog.show()
    }

    override fun onSocketConnected() {
        connectingDialog.dismiss()
        loading_layout.visibility = View.VISIBLE
    }

    override fun onStatusUpdate(statusMessage: String) {
        toast(statusMessage)
    }

    override fun onProgramingFinished(isSuccessful: Boolean) {
        toast("Is successfully programed? -> $isSuccessful")
    }

    override fun displayWaitMessage() {

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICK_CODE) {
            val fileUri = data?.data
            if (fileUri != null) {
                val input = requireContext().contentResolver.openInputStream(fileUri)
                val fileContent = input?.bufferedReader().use { it?.readText() }
                if (fileContent != null) {
                    fileLines.clear()
                    fileLines.addAll(fileContent.trimAndSplit())
                    if (fileLines.isNotEmpty()) {
                        SharedPrefsUtils.saveLastFileRead(requireContext(), fileContent)
                        presenter.extractFileData(fileLines)
                    }
                }
            } else toast(getString(R.string.no_file_picked))
        }
    }

    override fun onError(throwable: Throwable) {

    }


}