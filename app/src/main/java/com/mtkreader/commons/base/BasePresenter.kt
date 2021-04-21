package com.mtkreader.commons.base

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


interface AutoDisposePresenter {
    fun addDisposable(disposable: Disposable)
    fun clear()
    fun dispose()
}

interface ErrorHandlingPresenter {
    fun onErrorOccurred(throwable: Throwable)
}

abstract class BasePresenter(private val view: ErrorHandlingFragment) : AutoDisposePresenter,
    ErrorHandlingPresenter {
    private val compositeDisposable = CompositeDisposable()


    override fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun clear() {
        compositeDisposable.clear()
    }

    override fun dispose() {
        compositeDisposable.dispose()
    }

    override fun onErrorOccurred(throwable: Throwable) {
        throwable.printStackTrace()
        view.displayErrorPopup(throwable)
    }


}