package com.flyme2mars.hop.data.nearby

class PeerPresenceTracker(
    private val ttlMillis: Long = 45_000L,
    private val clock: () -> Long = { System.currentTimeMillis() },
) {
    private val seen = mutableMapOf<String, Long>()

    fun mark(id: String): Boolean {
        if (id.isBlank()) return false
        val added = !seen.containsKey(id)
        seen[id] = clock()
        return added
    }

    fun prune(): Boolean {
        val before = seen.size
        val now = clock()
        seen.entries.removeAll { now - it.value > ttlMillis }
        return seen.size != before
    }

    fun count(): Int {
        prune()
        return seen.size
    }

    fun ids(): Set<String> {
        prune()
        return seen.keys.toSet()
    }
}
