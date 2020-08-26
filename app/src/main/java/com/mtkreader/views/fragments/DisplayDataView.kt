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

class DisplayDataView : BaseMVPFragment<DisplayDataContract.Presenter>(), DisplayDataContract.View {

    private lateinit var data: CharArray

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val dataArg = arguments?.getCharArray(Const.Extras.DATA_EXTRA)
        if (dataArg != null)
            data = dataArg
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
    }

    private fun initPresenter() {
        presenter = DisplayDataPresenter(this)
    }

    private fun initViews() {}
}