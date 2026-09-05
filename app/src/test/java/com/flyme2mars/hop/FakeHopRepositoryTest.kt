package com.flyme2mars.hop

import com.flyme2mars.hop.data.FakeHopRepository
import com.flyme2mars.hop.data.HopProfile
import com.flyme2mars.hop.data.PostFilter
import com.flyme2mars.hop.data.PostKind
import com.flyme2mars.hop.data.formatElapsed
import com.flyme2mars.hop.data.formatRelativeTime
import com.flyme2mars.hop.ui.floor.buildFloorSubtitle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeHopRepositoryTest {
    private val profile = HopProfile(name = "Leah", room = "209", floor = "2")

    @Test
    fun filter_offer_hides_other_kinds() {
        val repo = FakeHopRepository()
        val offers = repo.floorPosts(PostFilter.Offer)
        assertTrue(offers.isNotEmpty())
        assertTrue(offers.all { it.kind == PostKind.Offer })
    }

    @Test
    fun add_post_lands_on_floor() {
        val repo = FakeHopRepository(seed = emptyList())
        repo.addPost(PostKind.Note, "Quiet hours", "Generator off after 11.", profile)
        val posts = repo.floorPosts(PostFilter.All)
        assertEquals(1, posts.size)
        assertEquals("Quiet hours", posts.first().title)
        assertTrue(repo.isOwn(posts.first()))
    }

    @Test
    fun claim_moves_to_history() {
        val repo = FakeHopRepository()
        val offer = repo.floorPosts(PostFilter.Offer).first()
        repo.claim(offer.id)
        assertTrue(repo.floorPosts(PostFilter.All).none { it.id == offer.id })
        assertTrue(repo.historyPosts().any { it.id == offer.id && it.claimed })
    }

    @Test
    fun remove_only_own() {
        val repo = FakeHopRepository(seed = emptyList())
        val mine = repo.addPost(PostKind.Ask, "Tape", "Need packing tape.", profile)
        assertFalse(repo.remove(mine.id, requesterId = "other"))
        assertTrue(repo.remove(mine.id, requesterId = FakeHopRepository.SELF_ID))
        assertTrue(repo.floorPosts().isEmpty())
    }

    @Test
    fun relative_and_elapsed_format() {
        val now = 1_700_000_000_000L
        assertEquals("2m", formatRelativeTime(now - 2 * 60_000, now))
        assertEquals("1h", formatRelativeTime(now - 60 * 60_000, now))
        assertEquals("1:05", formatElapsed(65))
        assertEquals("1:02:03", formatElapsed(3723))
    }

    @Test
    fun floor_subtitle_includes_nearby() {
        val subtitle = buildFloorSubtitle(profile, nearbyCount = 2)
        assertEquals("209 · Leah · 2 nearby", subtitle)
    }
}
