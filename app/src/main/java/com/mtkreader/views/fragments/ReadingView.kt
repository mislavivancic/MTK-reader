package com.mtkreader.views.fragments

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BaseMVPFragment
import com.mtkreader.contracts.ReadingContract
import com.mtkreader.presenters.ReadingPresenter
import com.mtkreader.utils.CommunicationUtil
import com.mtkreader.utils.SharedPrefsUtils
import com.mtkreader.views.dialogs.ConnectingDialog
import kotlinx.android.synthetic.main.fragment_reading.*

class ReadingView : BaseMVPFragment<ReadingContract.Presenter>(), ReadingContract.View {

    companion object {
        private const val TAG = "READING_FRAGMENT"

        private const val FIRST_LINE_TOKEN_FIRST = 13.toByte().toChar()
        private const val FIRST_LINE_TOKEN_SECOND = 10.toByte().toChar()
        private const val SECOND_LINE_TOKEN = 6.toByte().toChar()
        private const val SECOND_LINE_TOKEN_OTHER = 127.toByte().toChar()
    }

    private lateinit var connectedDevice: BluetoothDevice
    private lateinit var connectingDialog: Dialog
    private val data = mutableListOf<Char>()
    private val readingData = mutableListOf<Char>()
    private lateinit var socket: BluetoothSocket
    private var isReadingData = false

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
        startReading()
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

    private fun startReading() {
        presenter.connectToDevice(connectedDevice)
        connectingDialog = ConnectingDialog(requireContext())
        connectingDialog.show()
    }

    override fun onSocketConnected(socket: BluetoothSocket) {
        this.socket = socket

        connectingDialog.dismiss()
        presenter.readStream(this.socket)

        CommunicationUtil.writeToSocket(this.socket, Const.DeviceConstants.FIRST_INIT)
    }

    override fun onReceiveBytes(byte: Byte) {
        data.add(byte.toChar())
        readingData.add(byte.toChar())
        tv_data_read.append(byte.toChar().toString())
        println("${byte.toChar()} -> $byte ")
        if (data.contains(FIRST_LINE_TOKEN_FIRST) && data.contains(FIRST_LINE_TOKEN_SECOND) && !isReadingData) {
            data.clear()
            CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.SECOND_INIT)
        }
        if ((data.contains(SECOND_LINE_TOKEN) || data.contains(SECOND_LINE_TOKEN_OTHER)) && !isReadingData) {
            data.clear()
            CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.ACK)
            isReadingData = true
        }
        if (data.contains(Const.Tokens.END_TOKEN)) {
            socket.close()
            presenter.closeConnection()

            val dataBundle = Bundle().apply {

                putString(Const.Extras.DATA_EXTRA, readingData.joinToString(""))
            }
            SharedPrefsUtils.saveReadData(requireContext(), readingData.joinToString(""))
            data.clear()
            findNavController().navigate(R.id.navigateToDisplayDataView, dataBundle)
        }
    }

    override fun onError(throwable: Throwable) {
        displayErrorPopup(throwable)
    }

    override fun provideFragment(): Fragment = this

}