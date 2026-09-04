package com.flyme2mars.hop

import com.flyme2mars.hop.data.FakeHopRepository
import com.flyme2mars.hop.data.PostFilter
import com.flyme2mars.hop.data.PostKind
import com.flyme2mars.hop.data.authorInitials
import com.flyme2mars.hop.data.hasLeadingEdge
import com.flyme2mars.hop.data.matches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        val asks = posts.filter { it.matches(PostFilter.Asks) }
        val offers = posts.filter { it.matches(PostFilter.Offers) }
        val notes = posts.filter { it.matches(PostFilter.Notes) }
        assertEquals(posts.size, asks.size + offers.size + notes.size)
        assertTrue(asks.all { it.kind == PostKind.Ask })
        assertTrue(offers.all { it.kind == PostKind.Offer })
        assertTrue(notes.all { it.kind == PostKind.Note })
    }

    @Test
    fun leadingEdge_onlyAskAndOffer() {
        assertTrue(PostKind.Ask.hasLeadingEdge)
        assertTrue(PostKind.Offer.hasLeadingEdge)
        assertFalse(PostKind.Note.hasLeadingEdge)
    }

    @Test
    fun authorInitials_twoNames() {
        assertEquals("PK", authorInitials("Priya Kapoor"))
        assertEquals("YO", authorInitials("You"))
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
