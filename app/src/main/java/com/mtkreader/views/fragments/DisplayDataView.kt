package com.mtkreader.views.fragments

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebViewClient
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.mtkreader.commons.base.BaseMVPFragment
import com.mtkreader.contracts.DisplayDataContract
import com.mtkreader.presenters.DisplayDataPresenter
import com.mtkreader.utils.DataUtils
import com.mtkreader.utils.SharedPrefsUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_display_data.*

class DisplayDataView : BaseMVPFragment<DisplayDataContract.Presenter>(), DisplayDataContract.View {

    private lateinit var headerData: ByteArray
    private lateinit var bodyData: ByteArray
    private lateinit var htmlString: String

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val htmlExtra = arguments?.getString(Const.Extras.HTML_EXTRA)
        if (htmlExtra != null) {
            htmlString = htmlExtra
            return
        }
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
        if (this::htmlString.isInitialized) {
            displayData(htmlString)
        } else {
            presenter.processData(headerData, bodyData)
        }
    }

    private fun initPresenter() {
        presenter = DisplayDataPresenter(this)
    }

    private fun initViews() {
        requireActivity().title = getString(R.string.read_parameters)
        requireActivity().toolbar.setNavigationIcon(R.drawable.ic_back_white)

        webView.apply {
            webViewClient = WebViewClient()
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        }
    }

    override fun displayData(dataString: String) {
        val encodedHtml: String = Base64.encodeToString(dataString.toByteArray(Charsets.UTF_8), Base64.NO_PADDING)
        webView.loadData(encodedHtml, "text/html", "base64")

    }
}
