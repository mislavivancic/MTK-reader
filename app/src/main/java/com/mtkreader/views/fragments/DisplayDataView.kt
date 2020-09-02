package com.mtkreader.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BaseMVPFragment
import com.mtkreader.contracts.DisplayDataContract
import com.mtkreader.presenters.DisplayDataPresenter
import com.mtkreader.utils.DataUtils
import com.mtkreader.utils.SharedPrefsUtils

class DisplayDataView : BaseMVPFragment<DisplayDataContract.Presenter>(), DisplayDataContract.View {

    private lateinit var headerData: ByteArray
    private lateinit var bodyData: ByteArray

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val dataArg = arguments?.getString(Const.Extras.DATA_EXTRA)
        if (dataArg != null) {
            val (head, body) = DataUtils.extractHeaderAndBody(dataArg)
            headerData = head
            bodyData = body
        } else {
            val dataPrefs = SharedPrefsUtils.getReadData(requireContext())
            if (dataPrefs != null) {
                val (head, body) = DataUtils.extractHeaderAndBody(dataPrefs)
                headerData = head
                bodyData = body
            }
        }

        if (!this::bodyData.isInitialized) {
            requireActivity().finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_display_data, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initPresenter()
        initViews()

        println(bodyData.joinToString(""))

        presenter.processData(bodyData)
    }

    private fun initPresenter() {
        presenter = DisplayDataPresenter(this)
    }

    private fun initViews() {}

    override fun displayData(dataString: String) {
        println()
    }
}