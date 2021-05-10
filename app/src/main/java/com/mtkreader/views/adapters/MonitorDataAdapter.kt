package com.mtkreader.views.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mtkreader.views.fragments.monitor.MonitorEventLogFragment
import com.mtkreader.views.fragments.monitor.MonitorStatusFragment

class MonitorDataAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    val fragments = listOf(MonitorStatusFragment(), MonitorEventLogFragment())
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}
