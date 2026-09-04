package com.flyme2mars.hop

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.flyme2mars.hop.data.FakeHopRepository
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.data.HopPreferences
import com.flyme2mars.hop.data.HopProfile
import com.flyme2mars.hop.data.PostFilter
import com.flyme2mars.hop.data.PostKind

class HopViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = HopPreferences(application)
    private val repository = FakeHopRepository()

    var profile by mutableStateOf(preferences.load().profile)
        private set
    var onboarded by mutableStateOf(preferences.load().onboarded)
        private set
    var keepScreenOn by mutableStateOf(preferences.load().keepScreenOn)
        private set
    var filter by mutableStateOf(PostFilter.All)
        private set
    var floorPosts by mutableStateOf(repository.floorPosts(PostFilter.All))
        private set
    var historyPosts by mutableStateOf(repository.historyPosts())
        private set

    val nearbyCount: Int get() = repository.nearbyCount()

    fun updateFilter(value: PostFilter) {
        filter = value
        refresh()
    }

    fun completeLaunch(name: String, room: String, floor: String) {
        val next = HopProfile(name = name.trim(), room = room.trim(), floor = floor.trim())
        profile = next
        onboarded = true
        preferences.saveProfile(next, onboarded = true)
    }

    fun updateProfile(name: String, room: String, floor: String) {
        val next = HopProfile(name = name.trim(), room = room.trim(), floor = floor.trim())
        profile = next
        preferences.saveProfile(next, onboarded = onboarded)
    }

    fun updateKeepScreenOn(enabled: Boolean) {
        keepScreenOn = enabled
        preferences.saveKeepScreenOn(enabled)
    }

    fun addPost(kind: PostKind, title: String, body: String) {
        repository.addPost(kind, title, body, profile)
        refresh()
    }

    fun claim(post: HopPost) {
        repository.claim(post.id)
        refresh()
    }

    fun remove(post: HopPost): Boolean {
        val removed = repository.remove(post.id, FakeHopRepository.SELF_ID)
        refresh()
        return removed
    }

    fun isOwn(post: HopPost): Boolean = repository.isOwn(post)

    private fun refresh() {
        floorPosts = repository.floorPosts(filter)
        historyPosts = repository.historyPosts()
    }
}
