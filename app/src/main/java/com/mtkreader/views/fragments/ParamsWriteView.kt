package com.mtkreader.views.fragments

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BaseBluetoothFragment
import com.mtkreader.contracts.ParamsWriteContract
import com.mtkreader.presenters.ParamsWritePresenter
import com.mtkreader.trimAndSplit
import com.mtkreader.utils.SharedPrefsUtils
import com.mtkreader.views.dialogs.ConnectingDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_file_pick.*

class ParamsWriteView : BaseBluetoothFragment<ParamsWriteContract.Presenter>(), ParamsWriteContract.View {

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
        requireActivity().title = getString(R.string.write_parameters)
        requireActivity().toolbar.setNavigationIcon(R.drawable.ic_back_white)
        btn_pick_file.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/octet-stream"
            }.also {
                startActivityForResult(Intent.createChooser(it, getString(R.string.pick_mtk_file)), FILE_PICK_CODE)
            }
        }
        val lastFileData = SharedPrefsUtils.getLastFileRead(requireContext())
        if (lastFileData != null) {
            btn_last_file.isEnabled = true
            btn_last_file.setOnClickListener {
                presenter.extractFileData(lastFileData.trimAndSplit())
            }
        }

        btn_retry.setOnClickListener {
            val lastData = SharedPrefsUtils.getLastFileRead(requireContext())
            lastData?.let { presenter.extractFileData(it.trimAndSplit()) }
        }
    }

    override fun onDataReady() {
        presenter.connectToDevice(connectedDevice)
        connectingDialog = ConnectingDialog(requireContext())
        connectingDialog.show()
    }

    override fun onSocketConnected() {
        connectingDialog.dismiss()
        loading_layout.visibility = View.VISIBLE
        presenter.startCommunication()
    }

    override fun onStatusUpdate(statusMessage: String) {
        toast(statusMessage)
    }

    override fun onProgramingFinished() {
        loading_layout.visibility = View.GONE
        snack(getString(R.string.device_programmed), getString(R.string.ok), isError = false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICK_CODE) {
            val fileUri = data?.data
            if (fileUri != null) {
                val fileName = getFileName(fileUri)
                val extension = fileName?.split(".")?.getOrNull(1)
                if (extension != null && extension.toLowerCase() != "mtk") {
                    toast(getString(R.string.file_must_be))
                    return
                }
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


   private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = requireActivity().contentResolver.query(uri, null, null, null, null)
            cursor.use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result!!.substring(cut + 1)
            }
        }
        return result
    }

    override fun onError(throwable: Throwable) {
        connectingDialog.dismiss()
        loading_layout.visibility = View.GONE
        handleError(throwable) {
            val lastFileData = SharedPrefsUtils.getLastFileRead(requireContext())
            lastFileData?.let { presenter.extractFileData(it.trimAndSplit()) }
        }
        presenter.stopTimeout()
        presenter.tryReset()
    }
}
