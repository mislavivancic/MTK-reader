package com.mtkreader.commons.base

import androidx.fragment.app.Fragment

interface ErrorHandlingFragment {
    fun displayErrorPopup(throwable: Throwable)
}

open class BaseMVPFragment<T> : Fragment(), ErrorHandlingFragment where T : AutoDisposePresenter {

    protected lateinit var presenter: T

    override fun onStop() {
        presenter.clear()
        super.onStop()
    }

    override fun onDestroy() {
        presenter.dispose()
        super.onDestroy()
    }


    override fun displayErrorPopup(throwable: Throwable) {
        ErrorDialog(requireContext(), throwable.localizedMessage).show()
    }
}