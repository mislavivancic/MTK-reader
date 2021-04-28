package com.mtkreader.commons.base

import android.graphics.Color
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.mtkreader.R

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

    fun snack(text: String, actionText: String = getString(R.string.retry), isError: Boolean = true, action: (() -> Unit)? = null) {
        view?.let {
            val snackBar = Snackbar.make(it, text, Snackbar.LENGTH_INDEFINITE)
            with(snackBar) {
                setAction(actionText) { action?.invoke() }
                if (isError)
                    setActionTextColor(Color.RED)
                else
                    setActionTextColor(Color.YELLOW)
                //view.setBackgroundColor(Color.LTGRAY)
                show()
            }
        }
    }
}
