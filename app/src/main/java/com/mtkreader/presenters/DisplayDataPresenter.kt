package com.mtkreader.presenters

import com.mtkreader.commons.base.BasePresenter
import com.mtkreader.contracts.DisplayDataContract
import com.mtkreader.data.DataStructures
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject

class DisplayDataPresenter(private val view: DisplayDataContract.View) : BasePresenter(view),
    DisplayDataContract.Presenter, KoinComponent {

    private val displayService: DisplayDataContract.DisplayService by inject()
    private val processService: DisplayDataContract.ProcessService by inject()


    override fun processData(header: ByteArray, data: ByteArray) {
        val disposable = Single.fromCallable { processService.processData(header, data) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onDataProcessed, view::displayErrorPopup)

        addDisposable(disposable)
    }
    private fun onDataProcessed(data:DataStructures){
        val disposable = Single.fromCallable {displayService.generateHtml(data)  }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::displayData, view::displayErrorPopup)

        addDisposable(disposable)
    }


}
