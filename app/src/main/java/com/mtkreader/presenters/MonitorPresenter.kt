package com.mtkreader.presenters

import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BaseBluetoothPresenter
import com.mtkreader.contracts.MonitorContract
import com.mtkreader.utils.CommunicationUtil
import com.mtkreader.utils.DataUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.alexandroid.utils.mylogkt.logI
import org.koin.core.KoinComponent
import org.koin.core.inject

class MonitorPresenter(private val view: MonitorContract.View) : BaseBluetoothPresenter(view),
    MonitorContract.Presenter, KoinComponent {

    private val service: MonitorContract.Service by inject()
    private lateinit var waitMessageDisposable: Disposable

    override fun onByteReceive(byte: Byte) {
        stopTimeout()
        logI(byte.toString() +" -> "+ byte.toChar().toString(), customTag = "RECEIVED")
        view.onByte(byte)
        communicationManager.addData(byte)
    }

    override fun startCommunication() {
        waitMessageDisposable = Observable.fromCallable { communicate() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onReadoutDone, view::onError)
        addDisposable(waitMessageDisposable)
        readStream()
        initDeviceCommunication()
    }


    private fun communicate(): String {
        val headerMessage = communicationManager.waitForMessage()

        CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.SECOND_INIT)
        val message = communicationManager.waitForMessage()

        val monitorStatusCmd = DataUtils.createMessageObject("V")
        //CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.MONITOR_STATUS)
        CommunicationUtil.writeToSocket(socket, monitorStatusCmd.getBufferData().toByteArray())
        val monitorStatusMessage = communicationManager.waitForMessage(type = 1)
        if (monitorStatusMessage.status == Const.Data.COMPLETE) {
            val string = DataUtils.hexToAscii(DataUtils.byteArrayToHexString(monitorStatusMessage.getBufferData().toByteArray()))
            val dataMonitor = service.parseMonitor(string.substring(1))

        }
        //Thread.sleep(3000)
        //CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.MONITOR_STATUS)
        CommunicationUtil.writeToSocket(socket, monitorStatusCmd.getBufferData().toByteArray())
        val monitorStatusMessage2 = communicationManager.waitForMessage(type = 1)

        //Thread.sleep(4000)
        val readEventLogCommand = DataUtils.createMessageObject("Gs")
        CommunicationUtil.writeToSocket(socket, readEventLogCommand.getBufferData().toByteArray())
        val eventLog = communicationManager.waitForMessage(type = 1)
        return ""
    }

    private fun onReadoutDone(data: String) {

    }

}
