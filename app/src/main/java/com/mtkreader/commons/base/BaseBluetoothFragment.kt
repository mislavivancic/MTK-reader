package com.mtkreader.commons.base

import android.view.View
import com.github.ivbaranov.rxbluetooth.exceptions.ConnectionClosedException
import com.mtkreader.R
import com.mtkreader.exceptions.*
import kotlinx.android.synthetic.main.fragment_time.*
import java.io.IOException

open class BaseBluetoothFragment<T> : BaseMVPFragment<T>() where T : AutoDisposePresenter {

    protected fun handleError(throwable: Throwable, action: (() -> Unit)? = null) {
        throwable.printStackTrace()
        when (throwable) {
            is ConnectionClosedException -> {
                snack(getString(R.string.set_probe_in_connecting), action = {
                    action?.invoke()
                    toast(getString(R.string.retrying))
                    btn_retry.visibility = View.GONE
                })
                btn_retry.visibility = View.VISIBLE
            }
            is IOException -> {
                snack(getString(R.string.set_probe_in_connecting), action = {
                    action?.invoke()
                    toast(getString(R.string.retrying))
                    btn_retry.visibility = View.GONE
                })
                btn_retry.visibility = View.VISIBLE
            }
            is CommunicationException -> {
                snack(getString(R.string.switch_off_supply), action = {
                    action?.invoke()
                    toast(getString(R.string.retrying))
                    btn_retry.visibility = View.GONE
                })
                btn_retry.visibility = View.VISIBLE
            }
            is BccException -> {
                snack(getString(R.string.error_reading_parameters), action = {
                    action?.invoke()
                    toast(getString(R.string.retrying))
                    btn_retry.visibility = View.GONE
                })
                btn_retry.visibility = View.VISIBLE
            }
            is VerificationException -> {
                snack(getString(R.string.verification_error), action = {
                    action?.invoke()
                    toast(getString(R.string.retrying))
                    btn_retry.visibility = View.GONE
                })
                btn_retry.visibility = View.VISIBLE
            }
            is ProgrammingError -> {
                snack(getString(R.string.programming_error), action = {
                    action?.invoke()
                    toast(getString(R.string.retrying))
                    btn_retry.visibility = View.GONE
                })
                btn_retry.visibility = View.VISIBLE
            }
            is NotProgrammingModeException->{
                snack(getString(R.string.set_in_programming_mode), action = {
                    action?.invoke()
                    toast(getString(R.string.retrying))
                    btn_retry.visibility = View.GONE
                })
                btn_retry.visibility = View.VISIBLE
            }
            else -> displayErrorPopup(throwable)
        }
    }
}
