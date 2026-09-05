package com.flyme2mars.hop.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.hopDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "hop_settings",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, LEGACY_PREFS_NAME))
    },
)

private const val LEGACY_PREFS_NAME = "hop_prefs"

class HopSettingsStore(context: Context) {
    private val dataStore = context.applicationContext.hopDataStore

    val prefs: Flow<HopPrefs> = dataStore.data.map { it.toHopPrefs() }

    suspend fun snapshot(): HopPrefs = dataStore.data.first().toHopPrefs()

    suspend fun ensureSelfId(): String {
        val current = snapshot().selfId
        if (current.isNotBlank()) return current
        val created = UUID.randomUUID().toString()
        dataStore.edit { prefs ->
            if (prefs[KEY_SELF_ID].isNullOrBlank()) {
                prefs[KEY_SELF_ID] = created
            }
        }
        return snapshot().selfId.ifBlank { created }
    }

    suspend fun saveProfile(profile: HopProfile, onboarded: Boolean = true) {
        dataStore.edit { prefs ->
            prefs[KEY_NAME] = profile.name.trim()
            prefs[KEY_ROOM] = profile.room.trim()
            prefs[KEY_FLOOR] = profile.floor.trim()
            prefs[KEY_ONBOARDED] = onboarded
        }
    }

    suspend fun saveKeepScreenOn(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_KEEP_SCREEN_ON] = enabled }
    }

    suspend fun markSeeded() {
        dataStore.edit { prefs -> prefs[KEY_SEEDED] = true }
    }

    suspend fun saveBlackout(session: BlackoutSession?) {
        dataStore.edit { prefs ->
            if (session == null || session.startedAtMillis <= 0L) {
                prefs.remove(KEY_BLACKOUT_START)
                prefs.remove(KEY_BLACKOUT_STATUS)
            } else {
                prefs[KEY_BLACKOUT_START] = session.startedAtMillis
                prefs[KEY_BLACKOUT_STATUS] = session.status.name
            }
        }
    }

    suspend fun blackoutSession(): BlackoutSession? {
        val prefs = dataStore.data.first()
        val started = prefs[KEY_BLACKOUT_START] ?: return null
        if (started <= 0L) return null
        val status = prefs[KEY_BLACKOUT_STATUS]
            ?.let { runCatching { BlackoutStatus.valueOf(it) }.getOrNull() }
            ?: BlackoutStatus.None
        return BlackoutSession(startedAtMillis = started, status = status)
    }

    private fun Preferences.toHopPrefs(): HopPrefs = HopPrefs(
        profile = HopProfile(
            name = this[KEY_NAME].orEmpty(),
            room = this[KEY_ROOM].orEmpty(),
            floor = this[KEY_FLOOR].orEmpty(),
        ),
        onboarded = this[KEY_ONBOARDED] ?: false,
        keepScreenOn = this[KEY_KEEP_SCREEN_ON] ?: true,
        selfId = this[KEY_SELF_ID].orEmpty(),
        seeded = this[KEY_SEEDED] ?: false,
    )

    companion object {
        private val KEY_NAME = stringPreferencesKey("name")
        private val KEY_ROOM = stringPreferencesKey("room")
        private val KEY_FLOOR = stringPreferencesKey("floor")
        private val KEY_ONBOARDED = booleanPreferencesKey("onboarded")
        private val KEY_KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        private val KEY_SELF_ID = stringPreferencesKey("self_id")
        private val KEY_SEEDED = booleanPreferencesKey("seeded")
        private val KEY_BLACKOUT_START = longPreferencesKey("blackout_start")
        private val KEY_BLACKOUT_STATUS = stringPreferencesKey("blackout_status")
    }
}
