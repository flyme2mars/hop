package com.flyme2mars.hop.data.nearby

import com.flyme2mars.hop.data.NearbyPeer

class PeerPresenceTracker(
    private val ttlMillis: Long = 45_000L,
    private val clock: () -> Long = { System.currentTimeMillis() },
) {
    private data class SeenPeer(
        val peer: NearbyPeer,
        val lastSeen: Long,
    )

    private val seen = mutableMapOf<String, SeenPeer>()

    fun mark(id: String): Boolean {
        if (id.isBlank()) return false
        val existing = seen[id]
        val added = existing == null
        seen[id] = SeenPeer(
            peer = existing?.peer ?: NearbyPeer(id = id),
            lastSeen = clock(),
        )
        return added
    }

    fun updateIdentity(id: String, name: String, room: String): Boolean {
        if (id.isBlank()) return false
        val existing = seen[id]
        val next = (existing?.peer ?: NearbyPeer(id)).copy(name = name.trim(), room = room.trim())
        val changed = existing?.peer != next
        seen[id] = SeenPeer(peer = next, lastSeen = existing?.lastSeen ?: clock())
        return changed
    }

    fun prune(): Boolean {
        val before = seen.size
        val now = clock()
        seen.entries.removeAll { now - it.value.lastSeen > ttlMillis }
        return seen.size != before
    }

    fun count(): Int = peers().size

    fun ids(): Set<String> = peers().map { it.id }.toSet()

    fun peers(): List<NearbyPeer> {
        prune()
        return seen.values.map { it.peer }.sortedBy { it.label().lowercase() }
    }

    fun clear() {
        seen.clear()
    }
}
