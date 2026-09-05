package com.flyme2mars.hop

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.flyme2mars.hop.data.BlackoutSession
import com.flyme2mars.hop.data.BlackoutStatus
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.data.HopProfile
import com.flyme2mars.hop.data.NearbyAvailability
import com.flyme2mars.hop.data.NearbyState
import com.flyme2mars.hop.data.PostFilter
import com.flyme2mars.hop.data.PostKind
import com.flyme2mars.hop.data.claimedHistory
import com.flyme2mars.hop.data.nearby.HopNearbyController
import com.flyme2mars.hop.data.visibleFloor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class HopViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    private val container = (application as HopApplication).container
    private val settings = container.settings
    private val repository = container.repository

    private val nearby = HopNearbyController(
        context = application,
        scope = viewModelScope,
        floorProvider = { profile.floor },
        selfIdProvider = { selfId },
        profileProvider = { profile },
        snapshotProvider = { repository.snapshotForSync() },
        ingestRemote = { repository.ingestRemote(it) },
    )

    private val bootstrap = runBlocking {
        val prefs = settings.snapshot()
        prefs.copy(selfId = settings.ensureSelfId())
    }

    var profile by mutableStateOf(bootstrap.profile)
        private set
    var onboarded by mutableStateOf(bootstrap.onboarded)
        private set
    var keepScreenOn by mutableStateOf(bootstrap.keepScreenOn)
        private set
    var filter by mutableStateOf(PostFilter.All)
        private set
    private var allPosts: List<HopPost> = emptyList()
    var floorPosts by mutableStateOf<List<HopPost>>(emptyList())
        private set
    var historyPosts by mutableStateOf<List<HopPost>>(emptyList())
        private set
    var nearbyState by mutableStateOf(NearbyState(availability = NearbyAvailability.Checking))
        private set
    var selfId by mutableStateOf(bootstrap.selfId)
        private set
    private val restoredBlackout = runBlocking { settings.blackoutSession() }

    var blackoutStartedAt by mutableStateOf(
        savedStateHandle[KEY_BLACKOUT_START] ?: restoredBlackout?.startedAtMillis ?: 0L,
    )
        private set
    var blackoutStatus by mutableStateOf(
        savedStateHandle.get<String>(KEY_BLACKOUT_STATUS)
            ?.let { runCatching { BlackoutStatus.valueOf(it) }.getOrNull() }
            ?: restoredBlackout?.status
            ?: BlackoutStatus.None,
    )
        private set

    init {
        if (blackoutStartedAt > 0L) {
            savedStateHandle[KEY_BLACKOUT_START] = blackoutStartedAt
            savedStateHandle[KEY_BLACKOUT_STATUS] = blackoutStatus.name
        }
        viewModelScope.launch {
            selfId = settings.ensureSelfId()
            repository.ensureSeeded()
            settings.prefs.collectLatest { prefs ->
                profile = prefs.profile
                onboarded = prefs.onboarded
                keepScreenOn = prefs.keepScreenOn
                if (prefs.selfId.isNotBlank()) selfId = prefs.selfId
            }
        }
        viewModelScope.launch {
            repository.observePosts().collectLatest { posts ->
                allPosts = posts
                applyPosts()
                nearby.notifyBoardChanged()
            }
        }
        viewModelScope.launch {
            nearby.state.collectLatest { nearbyState = it }
        }
    }

    fun startNearby() {
        nearby.start()
    }

    fun stopNearby() {
        nearby.stop()
    }

    fun onNearbyPermissionsResult() {
        nearby.onPermissionsChanged()
        if (onboarded) nearby.start()
    }

    fun updateFilter(value: PostFilter) {
        filter = value
        applyPosts()
    }

    private fun applyPosts() {
        floorPosts = allPosts.visibleFloor(filter)
        historyPosts = allPosts.claimedHistory()
    }

    fun completeLaunch(name: String, room: String, floor: String) {
        val next = HopProfile(name = name.trim(), room = room.trim(), floor = floor.trim())
        profile = next
        onboarded = true
        viewModelScope.launch {
            settings.saveProfile(next, onboarded = true)
            nearby.start()
        }
    }

    fun updateProfile(name: String, room: String, floor: String) {
        val next = HopProfile(name = name.trim(), room = room.trim(), floor = floor.trim())
        profile = next
        viewModelScope.launch {
            settings.saveProfile(next, onboarded = onboarded)
            nearby.onFloorChanged()
            nearby.onPermissionsChanged()
        }
    }

    fun updateKeepScreenOn(enabled: Boolean) {
        keepScreenOn = enabled
        viewModelScope.launch { settings.saveKeepScreenOn(enabled) }
    }

    fun addPost(kind: PostKind, title: String, body: String) {
        viewModelScope.launch {
            repository.addPost(kind, title, body, profile, selfId)
        }
    }

    fun claim(post: HopPost) {
        viewModelScope.launch { repository.claim(post.id) }
    }

    fun remove(post: HopPost) {
        viewModelScope.launch { repository.remove(post.id, selfId) }
    }

    fun isOwn(post: HopPost): Boolean = repository.isOwn(post, selfId)

    fun enterBlackout() {
        if (blackoutStartedAt > 0L) return
        val started = System.currentTimeMillis()
        persistBlackout(started, BlackoutStatus.None)
    }

    fun exitBlackout() {
        persistBlackout(0L, BlackoutStatus.None)
    }

    fun updateBlackoutStatus(status: BlackoutStatus) {
        val started = if (blackoutStartedAt > 0L) blackoutStartedAt else System.currentTimeMillis()
        persistBlackout(started, status)
    }

    private fun persistBlackout(startedAt: Long, status: BlackoutStatus) {
        blackoutStartedAt = startedAt
        blackoutStatus = if (startedAt > 0L) status else BlackoutStatus.None
        savedStateHandle[KEY_BLACKOUT_START] = blackoutStartedAt
        savedStateHandle[KEY_BLACKOUT_STATUS] = blackoutStatus.name
        viewModelScope.launch {
            settings.saveBlackout(
                if (startedAt > 0L) {
                    BlackoutSession(startedAtMillis = startedAt, status = status)
                } else {
                    null
                },
            )
        }
    }

    override fun onCleared() {
        nearby.stop()
        super.onCleared()
    }

    companion object {
        private const val KEY_BLACKOUT_START = "blackout_start"
        private const val KEY_BLACKOUT_STATUS = "blackout_status"
    }
}
