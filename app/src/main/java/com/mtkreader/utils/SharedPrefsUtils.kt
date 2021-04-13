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

    fun getReadData(context: Context): String? {
        val prefs = context.getSharedPreferences(
            BuildConfig.APPLICATION_ID,
            Context.MODE_PRIVATE
        )
        return prefs.getString(Const.SharedPrefKeys.READ_DATA_KEY, null)
    }

    fun saveLastFileRead(context: Context, data: String) {
        context.getSharedPreferences(
            BuildConfig.APPLICATION_ID,
            Context.MODE_PRIVATE
        ).edit().putString(Const.SharedPrefKeys.LAST_FILE_READ_KEY, data).apply()
    }

    fun getLastFileRead(context: Context): String? {
        val prefs = context.getSharedPreferences(
            BuildConfig.APPLICATION_ID,
            Context.MODE_PRIVATE
        )
        return prefs.getString(Const.SharedPrefKeys.LAST_FILE_READ_KEY, null)
    }


}
