package com.mtkreader.commons.base

import androidx.fragment.app.Fragment

class BaseMVPFragment<T> : Fragment() where T : AutoDisposePresenter {

    protected lateinit var presenter: T

    override fun onStop() {
        presenter.clear()
        super.onStop()
    }

    override fun onDestroy() {
        presenter.dispose()
        super.onDestroy()
    }
}