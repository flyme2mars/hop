package com.flyme2mars.hop.data

import android.content.Context
import androidx.core.content.edit

class HopPreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): HopPrefs = HopPrefs(
        profile = HopProfile(
            name = prefs.getString(KEY_NAME, "").orEmpty(),
            room = prefs.getString(KEY_ROOM, "").orEmpty(),
            floor = prefs.getString(KEY_FLOOR, "").orEmpty(),
        ),
        onboarded = prefs.getBoolean(KEY_ONBOARDED, false),
        keepScreenOn = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true),
    )

    fun saveProfile(profile: HopProfile, onboarded: Boolean = true) {
        prefs.edit {
            putString(KEY_NAME, profile.name.trim())
            putString(KEY_ROOM, profile.room.trim())
            putString(KEY_FLOOR, profile.floor.trim())
            putBoolean(KEY_ONBOARDED, onboarded)
        }
    }

    fun saveKeepScreenOn(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_KEEP_SCREEN_ON, enabled) }
    }

    companion object {
        private const val PREFS_NAME = "hop_prefs"
        private const val KEY_NAME = "name"
        private const val KEY_ROOM = "room"
        private const val KEY_FLOOR = "floor"
        private const val KEY_ONBOARDED = "onboarded"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
    }
}
