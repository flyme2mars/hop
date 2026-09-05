package com.flyme2mars.hop

import com.flyme2mars.hop.data.HopProfile
import com.flyme2mars.hop.data.HopSyncCodec
import com.flyme2mars.hop.data.InMemoryHopRepository
import com.flyme2mars.hop.data.NearbyAvailability
import com.flyme2mars.hop.data.NearbyPeer
import com.flyme2mars.hop.data.NearbyState
import com.flyme2mars.hop.data.PostFilter
import com.flyme2mars.hop.data.PostKind
import com.flyme2mars.hop.data.defaultSeedPosts
import com.flyme2mars.hop.data.formatElapsed
import com.flyme2mars.hop.data.formatRelativeTime
import com.flyme2mars.hop.data.mergeRemotePost
import com.flyme2mars.hop.data.nearby.HopBleIds
import com.flyme2mars.hop.data.nearby.HopSyncFramer
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
    fun merge_equal_timestamp_keeps_local_and_unions_claim() {
        val repo = InMemoryHopRepository(seed = emptyList(), idFactory = { "local-1" })
        val local = repo.addPost(PostKind.Offer, "Rice", "One pot.", profile)
        repo.ingestRemote(listOf(local.copy(claimed = true, title = "Ignored")))
        val merged = repo.historyPosts().first()
        assertTrue(merged.claimed)
        assertEquals("Rice", merged.title)
    }

    @Test
    fun merge_newest_wins_and_is_idempotent() {
        val older = defaultSeedPosts(now = 1_000L).first().copy(
            title = "Old title",
            updatedAtMillis = 1_000L,
        )
        val newer = older.copy(title = "New title", claimed = true, updatedAtMillis = 2_000L)
        assertEquals(newer, mergeRemotePost(older, newer))
        assertEquals(newer, mergeRemotePost(newer, older))
        assertEquals(newer, mergeRemotePost(newer, newer))
        val repo = InMemoryHopRepository(seed = listOf(older))
        repo.ingestRemote(listOf(newer))
        repo.ingestRemote(listOf(newer))
        assertEquals("New title", repo.historyPosts().single().title)
        assertTrue(repo.historyPosts().single().claimed)
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
        val twoPeers = NearbyState(
            peers = listOf(
                NearbyPeer("aa1111", "Priya", "204"),
                NearbyPeer("bb2222", "Dev", "101"),
            ),
            availability = NearbyAvailability.Ready,
        )
        assertEquals("209 · Leah · 2 nearby", buildFloorSubtitle(profile, twoPeers))
        assertEquals(2, twoPeers.count)
        assertEquals(listOf("Priya · 204", "Dev · 101"), twoPeers.peers.map { it.label() })
        assertEquals(
            "209 · Leah · searching",
            buildFloorSubtitle(
                profile,
                NearbyState(availability = NearbyAvailability.Ready, searching = true),
            ),
        )
        assertEquals(
            "209 · Leah · Nobody nearby",
            buildFloorSubtitle(
                profile,
                NearbyState(availability = NearbyAvailability.Ready, searching = false),
            ),
        )
        assertEquals(
            "209 · Leah · needs Bluetooth",
            buildFloorSubtitle(profile, NearbyState(availability = NearbyAvailability.BluetoothOff)),
        )
        assertEquals(
            "209 · Leah · needs permission",
            buildFloorSubtitle(profile, NearbyState(availability = NearbyAvailability.PermissionNeeded)),
        )
    }

    @Test
    fun sync_codec_round_trip_full_posts() {
        val original = defaultSeedPosts(now = 1_700_000_000_000L).first().copy(
            body = "A longer body that used to be clipped in the toy snapshot.",
            updatedAtMillis = 1_700_000_100_000L,
        )
        val encoded = HopSyncCodec.encode(listOf(original))
        val decoded = HopSyncCodec.decode(encoded)
        assertEquals(1, decoded.size)
        assertEquals(original, decoded.first())
        assertTrue(String(encoded, Charsets.UTF_8).startsWith("HOP2"))
        val legacy = "HOP1\nseed-1|Offer|Rice|One pot.|Priya|204|priya|1700000000000|0"
        val fromV1 = HopSyncCodec.decode(legacy.toByteArray())
        assertEquals("seed-1", fromV1.single().id)
        assertEquals(1_700_000_000_000L, fromV1.single().updatedAtMillis)
        assertTrue(HopSyncCodec.decode("nope".toByteArray()).isEmpty())
    }

    @Test
    fun sync_framer_chunks_and_reassembles() {
        val payload = HopSyncCodec.encode(defaultSeedPosts(now = 1_700_000_000_000L))
        val chunks = HopSyncFramer.chunk(payload, maxChunkPayload = 40)
        assertTrue(chunks.size > 1)
        val assembler = HopSyncFramer.Assembler()
        var assembled: ByteArray? = null
        chunks.forEach { chunk ->
            assembled = assembler.add(chunk) ?: assembled
        }
        val complete = assembled
        assertTrue(complete != null)
        assertEquals(payload.toList(), complete?.toList())
        assertEquals(
            HopSyncCodec.decode(payload),
            HopSyncCodec.decode(complete ?: ByteArray(0)),
        )
        assertTrue(HopSyncFramer.isRequest(HopSyncFramer.request()))
        assertTrue(HopSyncFramer.chunk(ByteArray(0)).size == 1)
    }

    @Test
    fun peer_tracker_uses_stable_ids_and_ttl() {
        var now = 1_000L
        val tracker = PeerPresenceTracker(ttlMillis = 45_000, clock = { now })
        assertTrue(tracker.mark("peer-a"))
        assertFalse(tracker.mark("peer-a"))
        now = 20_000L
        assertEquals(1, tracker.count())
        now = 46_100L
        assertEquals(0, tracker.count())
        assertTrue(tracker.peers().isEmpty())
    }

    @Test
    fun ble_payload_matches_same_floor_only() {
        val payload = HopBleIds.presencePayload(floor = "2", selfId = "me-1")
        assertEquals(HopBleIds.PAYLOAD_SIZE, payload.size)
        assertTrue(HopBleIds.sameFloor(payload, "2"))
        assertTrue(HopBleIds.sameFloor(payload, " 2 "))
        assertFalse(HopBleIds.sameFloor(payload, "3"))
        assertTrue(HopBleIds.isSelf(payload, "me-1"))
        assertFalse(HopBleIds.isSelf(payload, "other"))
        assertEquals(HopBleIds.peerId(payload), HopBleIds.peerId(HopBleIds.presencePayload("2", "me-1")))
        assertTrue(HopBleIds.peerId(payload) != HopBleIds.peerId(HopBleIds.presencePayload("2", "other")))
    }
}
