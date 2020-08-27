package com.mtkreader.utils

import android.content.Context
import com.mtkreader.BuildConfig
import com.mtkreader.commons.Const

object SharedPrefsUtils {

    fun saveReadData(context: Context, data: String) {
        context.getSharedPreferences(
            BuildConfig.APPLICATION_ID,
            Context.MODE_PRIVATE
        ).edit().putString(Const.SharedPrefKeys.READ_DATA_KEY, data).apply()
    }

    fun getReadData(context: Context): CharArray? {
        val prefs = context.getSharedPreferences(
            BuildConfig.APPLICATION_ID,
            Context.MODE_PRIVATE
        )
        val data = prefs.getString(Const.SharedPrefKeys.READ_DATA_KEY, null)
        return data?.toCharArray()
    }


}