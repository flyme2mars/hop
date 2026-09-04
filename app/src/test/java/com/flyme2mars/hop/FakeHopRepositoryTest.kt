package com.flyme2mars.hop

import com.flyme2mars.hop.data.FakeHopRepository
import com.flyme2mars.hop.data.PostFilter
import com.flyme2mars.hop.data.PostKind
import com.flyme2mars.hop.data.matches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeHopRepositoryTest {
    @Test
    fun seedPosts_countIsSixToEight() {
        val posts = FakeHopRepository.seedPosts()
        assertTrue(posts.size in 6..8)
    }

    @Test
    fun seedPosts_coverAllKinds() {
        val kinds = FakeHopRepository.seedPosts().map { it.kind }.toSet()
        assertTrue(kinds.containsAll(PostKind.entries))
    }

    @Test
    fun filters_partitionSeedBoard() {
        val posts = FakeHopRepository.seedPosts()
        val requests = posts.filter { it.matches(PostFilter.Requests) }
        val offers = posts.filter { it.matches(PostFilter.Offers) }
        val alerts = posts.filter { it.matches(PostFilter.Alerts) }
        assertEquals(posts.size, requests.size + offers.size + alerts.size)
        assertTrue(requests.all { it.kind == PostKind.Request })
        assertTrue(offers.all { it.kind == PostKind.Offer })
        assertTrue(alerts.all { it.kind == PostKind.Alert })
    }

    @Test
    fun nearbyCount_isQuietDemoValue() {
        assertEquals(7, FakeHopRepository.NearbyCount)
    }

    @Test
    fun history_isPresent() {
        assertTrue(FakeHopRepository.seedHistory().isNotEmpty())
    }
}
