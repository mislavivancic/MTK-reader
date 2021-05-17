package com.mtkreader.views.fragments.monitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mtkreader.R
import kotlinx.android.synthetic.main.fragment_monitor_learn_cycle.*

class MonitorLearnCycleFragment : Fragment() {


    private var learnCycle = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_monitor_learn_cycle, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViews()
    }

    private fun initViews() {
        learn_cycle.text = learnCycle
    }

    fun update(learnCycle: String) {
        this.learnCycle = learnCycle
        if (learn_cycle != null)
            learn_cycle.text = this.learnCycle
    }

}
