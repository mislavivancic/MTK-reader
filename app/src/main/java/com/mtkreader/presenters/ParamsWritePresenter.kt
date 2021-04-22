package com.mtkreader.presenters

import android.util.Log
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BasePresenter
import com.mtkreader.contracts.DisplayDataContract
import com.mtkreader.contracts.ParamsWriteContract
import com.mtkreader.data.DataStructures
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject

class ParamsWritePresenter(private val view: ParamsWriteContract.View) : BasePresenter(view),
    ParamsWriteContract.Presenter, KoinComponent {

    private val fillDataStructuresService: ParamsWriteContract.FillDataStructuresService by inject()
    private val writeDataService: ParamsWriteContract.WriteDataService by inject()
    private val displayService: DisplayDataContract.DisplayService by inject()

    override fun extractFileData(fileLines: List<String>) {
        addDisposable(
            fillDataStructuresService.extractFileData(fileLines)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                //.subscribe(this::onDataStructuresFilled, this::onErrorOccurred)
                .subscribe(this::onDataStructuresFilledDisplay, this::onErrorOccurred)
        )
    }

    private fun onDataStructuresFilled(fileData: DataStructures) {
        addDisposable(
            writeDataService.generateStrings(fileData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onFinished, this::onErrorOccurred)
        )
    }


    private fun onDataStructuresFilledDisplay(data: DataStructures) {

        val disposable = Single.fromCallable {displayService.generateHtml(data)  }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onFinished, view::displayErrorPopup)

        addDisposable(disposable)



    }
    private fun onFinished(data: String) {
        Log.i(Const.Logging.PACK,"LOAD FROM FILE DISPLAY")
    }

}