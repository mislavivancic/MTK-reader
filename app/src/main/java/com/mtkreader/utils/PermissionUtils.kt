package com.mtkreader.utils

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionUtils {

    const val COARSE_LOCATION_REQUEST_CODE = 1


    fun hasCoarseLocation(fragment: Fragment): Boolean {
        return ContextCompat.checkSelfPermission(
            fragment.requireContext(),
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCoarseLocationPermission(fragment: Fragment) {
        fragment.requestPermissions(
            arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
            COARSE_LOCATION_REQUEST_CODE
        )
    }
}