package com.mtkreader.presenters

import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BaseBluetoothPresenter
import com.mtkreader.contracts.ReadingContract
import com.mtkreader.utils.CommunicationUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent

class ReadingPresenter(private val view: ReadingContract.View) : BaseBluetoothPresenter(view),
    ReadingContract.Presenter, KoinComponent {

    private lateinit var waitMessageDisposable: Disposable

    override fun onByteReceive(byte: Byte) {
        stopTimeout()
        communicationManager.addData(byte)
        view.onByte(byte)
    }

    override fun startCommunication() {
        waitMessageDisposable = Observable.fromCallable { communicate() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::onReadoutDone, view::onError)
        addDisposable(waitMessageDisposable)
        readStream()
        initDeviceCommunication()
    }


    private fun communicate(): String {
        var fullContent = ""

        val headerMessage = communicationManager.waitForMessage()
        fullContent += headerMessage.getBufferData().map { it.toChar() }.joinToString("")

        CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.SECOND_INIT)
        val message = communicationManager.waitForMessage()
        fullContent += message.getBufferData().map { it.toChar() }.joinToString("")


        CommunicationUtil.writeToSocket(socket, Const.DeviceConstants.ACK)
        val readoutDate = communicationManager.waitForMessage(type = 1)
        fullContent += readoutDate.getBufferData().map { it.toChar() }.joinToString("")


        return fullContent
    }

}
