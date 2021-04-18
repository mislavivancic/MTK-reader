package com.mtkreader.presenters

import com.mtkreader.commons.base.BasePresenter
import com.mtkreader.contracts.ParamsWriteContract
import com.mtkreader.data.DataStructures
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject

class ParamsWritePresenter(private val view: ParamsWriteContract.View) : BasePresenter(view),
    ParamsWriteContract.Presenter, KoinComponent {

    private val fillDataStructuresService: ParamsWriteContract.FillDataStructuresService by inject()
    private val writeDataService: ParamsWriteContract.WriteDataService by inject()

    override fun extractFileData(fileLines: List<String>) {
        addDisposable(
            fillDataStructuresService.extractFileData(fileLines)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onDataStructuresFilled, this::onErrorOccurred)
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

    private fun onFinished(data: String) {

    }

}