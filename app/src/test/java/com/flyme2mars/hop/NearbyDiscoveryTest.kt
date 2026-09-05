package com.flyme2mars.hop

import com.flyme2mars.hop.data.HopProfile
import com.flyme2mars.hop.data.HopSyncCodec
import com.flyme2mars.hop.data.NearbyAvailability
import com.flyme2mars.hop.data.NearbyPeer
import com.flyme2mars.hop.data.NearbyState
import com.flyme2mars.hop.data.defaultSeedPosts
import com.flyme2mars.hop.data.nearby.HopBleIds
import com.flyme2mars.hop.data.nearby.NearbyVerdict
import com.flyme2mars.hop.data.nearby.PeerPresenceTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NearbyDiscoveryTest {
    @Test
    fun status_ready_empty_is_searching_or_nobody() {
        val searching = NearbyState(availability = NearbyAvailability.Ready, searching = true)
        val alone = NearbyState(availability = NearbyAvailability.Ready, searching = false)
        assertTrue(searching.peers.isEmpty())
        assertTrue(alone.peers.isEmpty())
        assertEquals(0, searching.count)
        assertEquals(0, alone.count)
        assertEquals("searching", searching.statusLine())
        assertEquals("Nobody nearby", alone.statusLine())
        assertFalse(searching.statusLine().contains("nearby") && searching.peers.isEmpty() && searching.count > 0)
    }

    @Test
    fun status_never_shows_count_without_peers() {
        val two = NearbyState(
            peers = listOf(
                NearbyPeer("aa1111aa", "Priya", "204"),
                NearbyPeer("bb2222bb", "", ""),
            ),
            availability = NearbyAvailability.Ready,
        )
        assertEquals("2 nearby", two.statusLine())
        assertEquals(2, two.peers.size)
        assertEquals(two.count, two.peers.size)
        assertEquals("Priya · 204", two.peers[0].label())
        assertEquals("Phone nearby · bb2222", two.peers[1].label())
        val emptyReady = NearbyState(availability = NearbyAvailability.Ready, searching = true)
        assertFalse(emptyReady.statusLine().matches(Regex("\\d+ nearby")))
    }

    @Test
    fun unknown_peer_label_uses_short_id() {
        val peer = NearbyPeer(id = "cafebabedeadbeef")
        assertEquals("Phone nearby · cafeba", peer.label())
        assertEquals("cafeba", peer.shortId())
        assertEquals("Leah", NearbyPeer("id", name = "Leah").label())
        assertEquals("Phone nearby · 209", NearbyPeer("id", room = "209").label())
    }

    @Test
    fun evaluate_rejects_missing_short_wrong_floor_and_self() {
        val floor = "2"
        val self = "me-1"
        val full = HopBleIds.presencePayload(floor, self)
        assertEquals(NearbyVerdict.DropMissingPayload, HopBleIds.evaluate(null, floor, self).verdict)
        assertEquals(
            NearbyVerdict.DropShortPayload,
            HopBleIds.evaluate(full.copyOf(2), floor, self).verdict,
        )
        assertEquals(
            NearbyVerdict.DropShortPayload,
            HopBleIds.evaluate(full.copyOf(10), floor, self).verdict,
        )
        assertEquals(NearbyVerdict.DropSelf, HopBleIds.evaluate(full, floor, self).verdict)
        assertEquals(
            NearbyVerdict.DropWrongFloor,
            HopBleIds.evaluate(HopBleIds.presencePayload("3", "other"), floor, self).verdict,
        )
        val accepted = HopBleIds.evaluate(HopBleIds.presencePayload(floor, "other"), floor, self)
        assertEquals(NearbyVerdict.Accept, accepted.verdict)
        assertTrue(accepted.peerId.isNotBlank())
        assertTrue(accepted.reason.contains(accepted.peerId))
    }

    @Test
    fun four_byte_floor_token_rejects_truncated_same_prefix() {
        val full = HopBleIds.presencePayload("2", "other")
        val truncatedFloor = full.copyOfRange(0, 2) + ByteArray(10)
        assertEquals(12, truncatedFloor.size)
        assertFalse(HopBleIds.sameFloor(truncatedFloor, "2"))
        assertEquals(
            NearbyVerdict.DropWrongFloor,
            HopBleIds.evaluate(truncatedFloor, "2", "me-1").verdict,
        )
        assertEquals(4, HopBleIds.floorToken("2").size)
        assertEquals(8, HopBleIds.selfToken("me-1").size)
    }

    @Test
    fun tracker_keeps_identity_and_prunes() {
        var now = 0L
        val tracker = PeerPresenceTracker(ttlMillis = 10_000, clock = { now })
        assertTrue(tracker.mark("peer-a"))
        assertTrue(tracker.updateIdentity("peer-a", "Priya", "204"))
        assertFalse(tracker.updateIdentity("peer-a", "Priya", "204"))
        assertEquals(listOf("Priya · 204"), tracker.peers().map { it.label() })
        now = 10_001L
        assertEquals(0, tracker.count())
        tracker.mark("peer-b")
        tracker.clear()
        assertTrue(tracker.peers().isEmpty())
    }

    @Test
    fun sync_codec_carries_me_profile_without_breaking_posts() {
        val posts = defaultSeedPosts(now = 1_700_000_000_000L)
        val encoded = HopSyncCodec.encode(
            posts = posts,
            selfId = "me-1",
            profile = HopProfile(name = "Leah", room = "209", floor = "2"),
        )
        val text = String(encoded, Charsets.UTF_8)
        assertTrue(text.startsWith("HOP2"))
        assertTrue(text.contains("ME|me-1|Leah|209"))
        assertEquals(posts.size, HopSyncCodec.decode(encoded).size)
        val me = HopSyncCodec.decodeMe(encoded)
        assertEquals("me-1", me?.id)
        assertEquals("Leah", me?.name)
        assertEquals("209", me?.room)
        assertNull(HopSyncCodec.decodeMe(HopSyncCodec.encode(posts)))
        assertEquals(
            HopBleIds.peerIdFromSelfId("me-1"),
            HopBleIds.peerId(HopBleIds.presencePayload("2", "me-1")),
        )
    }
}
