package com.mtkreader.managers

import android.app.Activity
import android.view.View
import androidx.core.content.ContextCompat
import com.mtkreader.R
import com.mtkreader.commons.Const
import com.spiddekauga.android.ui.showcase.MaterialShowcaseSequence
import com.spiddekauga.android.ui.showcase.MaterialShowcaseView
import com.spiddekauga.android.ui.showcase.ShowcaseConfig

object ShowcaseManager {

    fun startTimeWriteShowcase(
        activity: Activity,
        firstButton: View,
        secondButton: View,
        thirdButton: View
    ) {
        val config = ShowcaseConfig(activity).apply {
            delay = 0
            backgroundColor = ContextCompat.getColor(activity, R.color.colorAccentTransparent)
            dismissBackgroundColor = ContextCompat.getColor(activity, R.color.colorPrimary)
            renderOverNavigationBar = true
        }


        MaterialShowcaseSequence(activity, Const.ShowCase.PROGRAM_TIME).apply {
            setConfig(config)
            setSingleUse(Const.ShowCase.PROGRAM_TIME)
            addSequenceItem(
                MaterialShowcaseView.Builder(activity)
                    .setTarget(firstButton)
                    .setTitleText(activity.getString(R.string.pick_time_hint))
                    .setDismissText(activity.getString(R.string.ok))
                    .build()
            )
            config.delay = 200
            addSequenceItem(
                secondButton,
                activity.getString(R.string.pick_date_hint),
                "",
                activity.getString(R.string.ok)
            )
            addSequenceItem(
                thirdButton,
                activity.getString(R.string.program_time_date_hint),
                "",
                activity.getString(R.string.ok)
            )
            show()
        }

    }

    fun startErrorShowcase(activity: Activity, errorButton: View) {
        MaterialShowcaseView.Builder(activity)
            .setTarget(errorButton)
            .setTitleText(activity.getString(R.string.click_here_retry))
            .setContentText("Optional content text")
            .setBackgroundColor(ContextCompat.getColor(activity, R.color.blood_red_trans))
            .setDismissBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary))
            .setDismissText(activity.getString(R.string.ok))
            .renderOverNavigationBar()
            .setSingleUse(Const.ShowCase.ERROR_TIME)
            .setDelay(200)
            .show()

    }

}