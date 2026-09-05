package com.flyme2mars.hop

import com.flyme2mars.hop.data.HopProfile
import com.flyme2mars.hop.data.HopSyncCodec
import com.flyme2mars.hop.data.InMemoryHopRepository
import com.flyme2mars.hop.data.NearbyAvailability
import com.flyme2mars.hop.data.NearbyState
import com.flyme2mars.hop.data.PostFilter
import com.flyme2mars.hop.data.PostKind
import com.flyme2mars.hop.data.defaultSeedPosts
import com.flyme2mars.hop.data.formatElapsed
import com.flyme2mars.hop.data.formatRelativeTime
import com.flyme2mars.hop.data.mergeRemotePost
import com.flyme2mars.hop.data.nearby.HopBleIds
import com.flyme2mars.hop.data.nearby.PeerPresenceTracker
import com.flyme2mars.hop.ui.floor.buildFloorSubtitle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HopBoardTest {
    private val profile = HopProfile(name = "Leah", room = "209", floor = "2")

    @Test
    fun filter_offer_hides_other_kinds() {
        val repo = InMemoryHopRepository()
        val offers = repo.floorPosts(PostFilter.Offer)
        assertTrue(offers.isNotEmpty())
        assertTrue(offers.all { it.kind == PostKind.Offer })
    }

    @Test
    fun add_post_lands_on_floor() {
        val repo = InMemoryHopRepository(seed = emptyList(), idFactory = { "local-1" })
        repo.addPost(PostKind.Note, "Quiet hours", "Generator off after 11.", profile)
        val posts = repo.floorPosts(PostFilter.All)
        assertEquals(1, posts.size)
        assertEquals("Quiet hours", posts.first().title)
        assertTrue(repo.isOwn(posts.first()))
    }

    @Test
    fun claim_moves_to_history() {
        val repo = InMemoryHopRepository()
        val offer = repo.floorPosts(PostFilter.Offer).first()
        repo.claim(offer.id)
        assertTrue(repo.floorPosts(PostFilter.All).none { it.id == offer.id })
        assertTrue(repo.historyPosts().any { it.id == offer.id && it.claimed })
    }

    @Test
    fun remove_only_own() {
        val repo = InMemoryHopRepository(seed = emptyList(), idFactory = { "mine" })
        val mine = repo.addPost(PostKind.Ask, "Tape", "Need packing tape.", profile)
        assertFalse(repo.remove(mine.id, requesterId = "other"))
        assertTrue(repo.remove(mine.id, requesterId = InMemoryHopRepository.SELF_ID))
        assertTrue(repo.floorPosts().isEmpty())
    }

    @Test
    fun seed_only_once() {
        val repo = InMemoryHopRepository(seed = emptyList(), alreadySeeded = false)
        repo.ensureSeeded(defaultSeedPosts(now = 1_700_000_000_000L))
        repo.ensureSeeded(defaultSeedPosts(now = 1_800_000_000_000L))
        assertEquals(4, repo.floorPosts().size)
        assertTrue(repo.isSeeded())
        repo.remove("seed-1", requesterId = "priya")
        val afterDelete = InMemoryHopRepository(seed = repo.allPosts(), alreadySeeded = true)
        afterDelete.ensureSeeded()
        assertEquals(3, afterDelete.floorPosts().size)
    }

    @Test
    fun merge_remote_claim_keeps_local_copy() {
        val repo = InMemoryHopRepository(seed = emptyList(), idFactory = { "local-1" })
        val local = repo.addPost(PostKind.Offer, "Rice", "One pot.", profile)
        repo.ingestRemote(listOf(local.copy(claimed = true, title = "Ignored")))
        val merged = repo.historyPosts().first()
        assertTrue(merged.claimed)
        assertEquals("Rice", merged.title)
    }

    @Test
    fun merge_remote_inserts_unknown_post() {
        val repo = InMemoryHopRepository(seed = emptyList())
        val remote = defaultSeedPosts().first()
        repo.ingestRemote(listOf(remote))
        assertEquals(remote.id, repo.floorPosts().first().id)
        assertEquals(remote, mergeRemotePost(null, remote))
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
    fun floor_subtitle_is_honest() {
        assertEquals(
            "209 · Leah · 2 nearby",
            buildFloorSubtitle(profile, NearbyState(count = 2, availability = NearbyAvailability.Ready)),
        )
        assertEquals(
            "209 · Leah · 0 nearby",
            buildFloorSubtitle(profile, NearbyState(count = 0, availability = NearbyAvailability.Ready)),
        )
        assertEquals(
            "209 · Leah · Bluetooth off",
            buildFloorSubtitle(profile, NearbyState(availability = NearbyAvailability.BluetoothOff)),
        )
        assertEquals(
            "209 · Leah · Nearby permission needed",
            buildFloorSubtitle(profile, NearbyState(availability = NearbyAvailability.PermissionNeeded)),
        )
    }

    @Test
    fun sync_codec_round_trip() {
        val original = defaultSeedPosts(now = 1_700_000_000_000L).first()
        val encoded = HopSyncCodec.encode(listOf(original))
        val decoded = HopSyncCodec.decode(encoded)
        assertEquals(1, decoded.size)
        assertEquals(original.id, decoded.first().id)
        assertEquals(original.kind, decoded.first().kind)
        assertEquals(original.title, decoded.first().title)
        assertTrue(HopSyncCodec.decode("nope".toByteArray()).isEmpty())
    }

    @Test
    fun peer_tracker_expires() {
        var now = 1_000L
        val tracker = PeerPresenceTracker(ttlMillis = 20_000, clock = { now })
        tracker.mark("aa:bb")
        now = 10_000L
        assertEquals(1, tracker.count())
        now = 22_000L
        assertEquals(0, tracker.count())
    }

    @Test
    fun ble_payload_matches_same_floor_only() {
        val payload = HopBleIds.presencePayload(floor = "2", selfId = "me-1")
        assertTrue(HopBleIds.sameFloor(payload, "2"))
        assertTrue(HopBleIds.sameFloor(payload, " 2 "))
        assertFalse(HopBleIds.sameFloor(payload, "3"))
        assertTrue(HopBleIds.isSelf(payload, "me-1"))
        assertFalse(HopBleIds.isSelf(payload, "other"))
    }
}
