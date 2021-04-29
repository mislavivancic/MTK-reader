package com.mtkreader.presenters

import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BaseBluetoothPresenter
import com.mtkreader.contracts.MonitorContract
import com.mtkreader.utils.CommunicationUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject

class MonitorPresenter(private val view: MonitorContract.View) : BaseBluetoothPresenter(view),
    MonitorContract.Presenter, KoinComponent {

    private val service: MonitorContract.Service by inject()
    private lateinit var waitMessageDisposable: Disposable

    override fun onByteReceive(byte: Byte) {
        stopTimeout()
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


        CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.ACK)
        val readoutDate = communicationManager.waitForMessage(type = 1)

        return ""
    }

    private fun onReadoutDone(data: String) {

    }

}
