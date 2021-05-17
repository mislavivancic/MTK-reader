package com.mtkreader.views.fragments.monitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mtkreader.R
import kotlinx.android.synthetic.main.fragment_monitor_event_log.*


class MonitorEventLogFragment : Fragment() {


    private var eventLogData = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_monitor_event_log, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViews()
    }

    fun update(eventLogData: String) {
        this.eventLogData = eventLogData
        if (event_log != null)
            event_log.text = this.eventLogData
    }

    private fun initViews() {
        event_log.text = eventLogData
    }

}
