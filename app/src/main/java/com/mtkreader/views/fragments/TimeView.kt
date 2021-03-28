package com.mtkreader.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebViewClient
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BaseMVPFragment
import com.mtkreader.contracts.DisplayDataContract
import com.mtkreader.contracts.TimeContract
import com.mtkreader.presenters.DisplayDataPresenter
import com.mtkreader.utils.DataUtils
import com.mtkreader.utils.SharedPrefsUtils
import kotlinx.android.synthetic.main.fragment_display_data.*
import kotlinx.android.synthetic.main.fragment_display_time_data.*

class TimeView : BaseMVPFragment<TimeContract.Presenter>(), TimeContract.View {

    private lateinit var bodyData: ByteArray

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val dataArg = arguments?.getString(Const.Extras.DATA_EXTRA)
        if (dataArg != null) {
            // bodyData = body
        }

        bodyData = byteArrayOf(1, 2)
        if (!this::bodyData.isInitialized) {
            requireActivity().finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_display_time_data, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initPresenter()

        println(bodyData.joinToString(""))

        //presenter.processData(bodyData)
    }

    private fun initPresenter() {
        // presenter = DisplayDataPresenter(this)
    }


    override fun displayData(dataString: String) {
        tv_device_date.text = dataString
    }
}