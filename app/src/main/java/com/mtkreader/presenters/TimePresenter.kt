package com.mtkreader.presenters

import com.mtkreader.commons.Const
import com.mtkreader.commons.Const.BluetoothConstants.TIME_QUERY_INTERVAL
import com.mtkreader.commons.base.BaseBluetoothPresenter
import com.mtkreader.contracts.TimeContract
import com.mtkreader.data.DeviceDate
import com.mtkreader.data.DeviceTime
import com.mtkreader.utils.CommunicationUtil
import com.mtkreader.utils.DataUtils
import com.mtkreader.views.adapters.DeviceOperation
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit

class TimePresenter(private val view: TimeContract.View, private val deviceOperation: DeviceOperation) : BaseBluetoothPresenter(view),
    TimeContract.Presenter, KoinComponent {


    private val service: TimeContract.Service by inject()
    private lateinit var getTimeDisposable: Disposable
    private lateinit var waitMessageDisposable: Disposable

    private lateinit var deviceTime: DeviceTime
    private lateinit var deviceDate: DeviceDate

    private lateinit var timeDatePair: Pair<String, String>

    override fun startCommunication() {
        waitMessageDisposable = Observable.fromCallable {
            val headerMessage = communicationManager.waitForMessage()
            val hardwareVersion = DataUtils.getHardwareVersion(headerMessage.getBufferData().joinToString("").toByteArray())


            CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.SECOND_INIT)
            var message = communicationManager.waitForMessage()
            return@fromCallable hardwareVersion

        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onCommunicationStarted, view::onError)
        addDisposable(waitMessageDisposable)
        readStream()
        initDeviceCommunication()
    }

    override fun onByteReceive(byte: Byte) {
        stopTimeout()
        communicationManager.addData(byte)
    }

    private fun onCommunicationStarted(hardwareVersion: Int) {
        when (deviceOperation) {
            DeviceOperation.TIME_READ -> getTime(hardwareVersion)
            DeviceOperation.TIME_SET -> setTime(hardwareVersion)
            else -> getTime(hardwareVersion)
        }
    }

    private fun getTime(hardwareVersion: Int) {
        getTimeDisposable = Observable.interval(0, TIME_QUERY_INTERVAL, TimeUnit.MILLISECONDS).doOnNext {
            timeDatePair = communicateTimeRead(hardwareVersion)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { view.onError(it) }
            .doOnNext { view.displayTimeData(timeDatePair) }
            .subscribe()
        addDisposable(getTimeDisposable)
    }


    private fun communicateTimeRead(hardwareVersion: Int): Pair<String, String> {
        CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.GET_TIME)
        val timeMessage = communicationManager.waitForMessage()
        return service.extractTimeData(context, timeMessage.getBufferData().map { it.toChar() }, hardwareVersion)
    }

    private fun setTime(hardwareVersion: Int) {
        addDisposable(Observable.fromCallable { communicateTimeWrite() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(view::onTimeWriteResult)
            .doOnComplete { getTime(hardwareVersion) }
            .subscribe(view::onTimeWriteResult, view::onError))
    }

    private fun communicateTimeWrite(): Boolean {
        val setTimeCommand = service.generateTimeWriteMessage(deviceTime, deviceDate)
        CommunicationUtil.writeToSocket(socket, setTimeCommand.buffer.take(setTimeCommand.count).toByteArray())
        val timeSetResponse = communicationManager.waitForMessage()
        return timeSetResponse.status == Const.Data.ACK
    }

    override fun stopTimeFetch() {
        getTimeDisposable.dispose()
    }

    override fun setTimeDate(time: DeviceTime, deviceDate: DeviceDate) {
        this.deviceTime = time
        this.deviceDate = deviceDate
    }

}
