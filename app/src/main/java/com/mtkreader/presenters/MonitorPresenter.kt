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

    override fun startCommunication(req:Int) { //0 status //1 eventlog //2 learn
        waitMessageDisposable = Observable.fromCallable { communicate(req) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onReadoutDone, view::onError)
        addDisposable(waitMessageDisposable)
        readStream()
        initDeviceCommunication()
    }


    private fun communicate(req:Int): String {
        val headerMessage = communicationManager.waitForMessage()

        CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.SECOND_INIT)
        val message = communicationManager.waitForMessage()
        when(req) {

            0 -> {
                val monitorStatusCmd = DataUtils.createMessageObject("V")
                //CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.MONITOR_STATUS)
                CommunicationUtil.writeToSocket(socket, monitorStatusCmd.getBufferData().toByteArray())
                val monitorStatusMessage = communicationManager.waitForMessage(type = 1)
                if (monitorStatusMessage.status == Const.Data.COMPLETE) {
                    val string = DataUtils.hexToAscii(DataUtils.byteArrayToHexString(monitorStatusMessage.getBufferData().toByteArray()))
                    val dataMonitor = service.parseMonitor(string.substring(1))
                    val s = dataMonitor.dispStatus()
                    view.onDispStatus(s)
                }
            }
            1 -> {
                val readEventLogCommand = DataUtils.createMessageObject("Gs")
                CommunicationUtil.writeToSocket(socket, readEventLogCommand.getBufferData().toByteArray())
                val eventLog = communicationManager.waitForMessage(type = 1)
                if (eventLog.status == Const.Data.COMPLETE) {
                    val string = DataUtils.hexToAscii(DataUtils.byteArrayToHexString(eventLog.getBufferData().toByteArray()))
                    val dataMonitor = service.parseMonitor(string.substring(1))
                    service.SaveLogEvent()
                    val s = dataMonitor.disp_eventlog
                    view.onDispStatus(s)
                }
            }
            2 -> {

                val readLearnCommand = DataUtils.createMessageObject("Gh")
                CommunicationUtil.writeToSocket(socket, readLearnCommand.getBufferData().toByteArray())
                val learnLog = communicationManager.waitForMessage(type = 1)
                if (learnLog.status == Const.Data.COMPLETE) {
                    val string = DataUtils.hexToAscii(DataUtils.byteArrayToHexString(learnLog.getBufferData().toByteArray()))
                    val dataMonitor = service.parseMonitor(string.substring(1))
                    val s = dataMonitor.disp_learncycle
                    view.onDispStatus(s)
                }

            }
        }
        return ""
    }

    private fun onReadoutDone(data: String) {

    }

}
