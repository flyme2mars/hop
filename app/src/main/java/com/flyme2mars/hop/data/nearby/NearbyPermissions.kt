package com.flyme2mars.hop.data.nearby

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.flyme2mars.hop.R

object NearbyPermissions {
    fun required(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }

    fun hasAll(context: Context): Boolean =
        required().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

    fun rationaleIds(): List<Int> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                R.string.perm_bluetooth_scan,
                R.string.perm_bluetooth_advertise,
                R.string.perm_bluetooth_connect,
            )
        } else {
            listOf(R.string.perm_location_legacy)
        }
}
