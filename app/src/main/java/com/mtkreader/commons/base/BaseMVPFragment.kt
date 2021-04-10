package com.mtkreader.commons.base

import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_connect.*

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
        throwable.printStackTrace()
        ErrorDialog(
            requireContext(), throwable.localizedMessage ?: ""
        ) {
            requireActivity().onBackPressed()
        }.show()
    }

    fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}