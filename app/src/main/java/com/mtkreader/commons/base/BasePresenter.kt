package com.mtkreader.commons.base

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


interface AutoDisposePresenter {
    fun addDisposable(disposable: Disposable)
    fun clear()
    fun dispose()
}

class BasePresenter : AutoDisposePresenter {
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


}