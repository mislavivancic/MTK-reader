package com.mtkreader.presenters

import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BaseBluetoothPresenter
import com.mtkreader.contracts.MonitorContract
import com.mtkreader.data.MonitorStatus
import com.mtkreader.utils.CommunicationUtil
import com.mtkreader.utils.DataUtils
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit

class MonitorPresenter(private val view: MonitorContract.View) : BaseBluetoothPresenter(view),
    MonitorContract.Presenter, KoinComponent {

    companion object {
        private const val MONITOR_STATUS_QUERY_INTERVAL = 3000L
    }

    private val service: MonitorContract.Service by inject()
    private lateinit var waitMessageDisposable: Disposable
    private lateinit var getMonitorStatusDisposable: Disposable

    private var monitorStatus: MonitorStatus? = null

    override fun onByteReceive(byte: Byte) {
        stopTimeout()
        view.onByte(byte)
        communicationManager.addData(byte)
    }

    override fun startCommunication() {
        waitMessageDisposable = Completable.fromCallable {
            val headerMessage = communicationManager.waitForMessage()

            CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.SECOND_INIT)
            val message = communicationManager.waitForMessage()
            return@fromCallable true
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onCommunicationStarted, view::onError)
        addDisposable(waitMessageDisposable)
        readStream()
        initDeviceCommunication()
    }

    private fun onCommunicationStarted() {
        getMonitorStatusDisposable = Observable.interval(0, MONITOR_STATUS_QUERY_INTERVAL, TimeUnit.MILLISECONDS)
            .doOnNext { monitorStatus = readMonitorStatus() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                monitorStatus?.let {
                    view.displayStatus(monitorStatus!!)
                    view.onStatusReadingInProgress()
                }

            }
            .doOnError(view::onError)
            .subscribe()
        addDisposable(getMonitorStatusDisposable)
    }


    override fun readEventLog() {
        getMonitorStatusDisposable.dispose()
        waitMessageDisposable.dispose()
        waitMessageDisposable = Single.fromCallable {
            val readEventLogCommand = DataUtils.createMessageObject("Gs")
            CommunicationUtil.writeToSocket(socket, readEventLogCommand.getBufferData().toByteArray())
            val eventLog = communicationManager.waitForMessage(type = 1)
            if (eventLog.status == Const.Data.COMPLETE) {
                val string = DataUtils.hexToAscii(DataUtils.byteArrayToHexString(eventLog.getBufferData().toByteArray()))
                val dataMonitor = service.parseMonitor(string.substring(1))
                service.SaveLogEvent()
                return@fromCallable dataMonitor.disp_eventlog
            }
            return@fromCallable ""
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::displayEventLog, view::onError)
        addDisposable(waitMessageDisposable)
    }

    override fun readLearn() {
        getMonitorStatusDisposable.dispose()
        waitMessageDisposable.dispose()
        waitMessageDisposable = Single.fromCallable {
            val readLearnCommand = DataUtils.createMessageObject("Gh")
            CommunicationUtil.writeToSocket(socket, readLearnCommand.getBufferData().toByteArray())
            val learnLog = communicationManager.waitForMessage(type = 1)
            if (learnLog.status == Const.Data.COMPLETE) {
                val string = DataUtils.hexToAscii(DataUtils.byteArrayToHexString(learnLog.getBufferData().toByteArray()))
                val dataMonitor = service.parseMonitor(string.substring(1))
                return@fromCallable dataMonitor.disp_learncycle
            }
            return@fromCallable ""
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::displayLearn, view::onError)
        addDisposable(waitMessageDisposable)
    }


    private fun readMonitorStatus(): MonitorStatus {
        val monitorStatusCmd = DataUtils.createMessageObject("V")
        CommunicationUtil.writeToSocket(socket, monitorStatusCmd.getBufferData().toByteArray())
        val monitorStatusMessage = communicationManager.waitForMessage(type = 1)
        if (monitorStatusMessage.status == Const.Data.COMPLETE) {
            val string = DataUtils.hexToAscii(DataUtils.byteArrayToHexString(monitorStatusMessage.getBufferData().toByteArray()))
            val dataMonitor = service.parseMonitor(string.substring(1))
            return dataMonitor.dispStatus()
        } else {
            throw Exception("a") // TODO add exception
        }
    }

}
