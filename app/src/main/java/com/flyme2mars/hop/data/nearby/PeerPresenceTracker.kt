package com.flyme2mars.hop.data.nearby

class PeerPresenceTracker(
    private val ttlMillis: Long = 20_000L,
    private val clock: () -> Long = { System.currentTimeMillis() },
) {
    private val seen = mutableMapOf<String, Long>()

    fun mark(id: String) {
        if (id.isBlank()) return
        seen[id] = clock()
    }

    fun prune() {
        val now = clock()
        seen.entries.removeAll { now - it.value > ttlMillis }
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
