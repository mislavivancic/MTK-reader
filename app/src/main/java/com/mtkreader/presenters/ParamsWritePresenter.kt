package com.mtkreader.presenters

import com.mtkreader.commons.base.BasePresenter
import com.mtkreader.contracts.ParamsWriteContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject

class ParamsWritePresenter(private val view: ParamsWriteContract.View) : BasePresenter(view),
    ParamsWriteContract.Presenter, KoinComponent {

    private val service: ParamsWriteContract.Service by inject()

    override fun extractFileData(fileLines: List<String>) {
        addDisposable(
            service.extractFileData(fileLines)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::success, this::onErrorOccurred)
        )
    }

    private fun success() {

    }

}