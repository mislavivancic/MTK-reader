package com.mtkreader.views.fragments.monitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mtkreader.R
import com.mtkreader.data.MonitorStatus
import com.mtkreader.data.RelayStatus
import kotlinx.android.synthetic.main.fragment_monitor_status.*
import kotlinx.android.synthetic.main.monitor_status_info_layout.*
import kotlinx.android.synthetic.main.relay_status_layout.*


class MonitorStatusFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_monitor_status, container, false)
    }

    fun updateMonitor(monitorStatus: MonitorStatus) {
        paramFile.text = monitorStatus.parameterFile
        deviceTime.text = monitorStatus.deviceTime
        rtc.text = monitorStatus.RTC
        timeInOperation.text = monitorStatus.timeInOperation
        outages.text = monitorStatus.outageCount
        utf.text = monitorStatus.utfV
        lastTlg.text = monitorStatus.lastTelegram


        for ((i, relayStatus) in monitorStatus.relayStatuses.withIndex()) {
            when (i) {
                0 -> fillRelay1(relayStatus)
                1 -> fillRelay2(relayStatus)
                2 -> fillRelay3(relayStatus)
                3 -> fillRelay4(relayStatus)
            }
        }

        info.visibility = View.VISIBLE
        relay_status_info.visibility = View.VISIBLE
    }

    private fun fillRelay1(relayStatus: RelayStatus) {
        relayName1.text = relayStatus.relayNum
        wiperActive1.text = relayStatus.wiper
        learn1.text = relayStatus.learn
        programs1.text = relayStatus.programs
        loop1.text = relayStatus.loop
        transmiterCount1.text = relayStatus.transmitterFail
        switchCount1.text = relayStatus.switchingCount
    }

    private fun fillRelay2(relayStatus: RelayStatus) {
        relayName2.text = relayStatus.relayNum
        wiperActive2.text = relayStatus.wiper
        learn2.text = relayStatus.learn
        programs2.text = relayStatus.programs
        loop2.text = relayStatus.loop
        transmiterCount2.text = relayStatus.transmitterFail
        switchCount2.text = relayStatus.switchingCount
    }

    private fun fillRelay3(relayStatus: RelayStatus) {
        relayName3.text = relayStatus.relayNum
        wiperActive3.text = relayStatus.wiper
        learn3.text = relayStatus.learn
        programs3.text = relayStatus.programs
        loop3.text = relayStatus.loop
        transmiterCount3.text = relayStatus.transmitterFail
        switchCount3.text = relayStatus.switchingCount
    }

    private fun fillRelay4(relayStatus: RelayStatus) {
        relayName4.text = relayStatus.relayNum
        wiperActive4.text = relayStatus.wiper
        learn4.text = relayStatus.learn
        programs4.text = relayStatus.programs
        loop4.text = relayStatus.loop
        transmiterCount4.text = relayStatus.transmitterFail
        switchCount4.text = relayStatus.switchingCount
    }
}
