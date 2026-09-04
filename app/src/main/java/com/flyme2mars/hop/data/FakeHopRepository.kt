package com.flyme2mars.hop.data

class FakeHopRepository(
    seed: List<HopPost> = defaultSeed(),
    private val clock: () -> Long = { System.currentTimeMillis() },
) {
    private val floor = seed.toMutableList()
    private val history = mutableListOf<HopPost>()
    private var nextId = seed.size + 1

    fun floorPosts(filter: PostFilter = PostFilter.All): List<HopPost> {
        val visible = floor.filterNot { it.claimed }
        return when (filter) {
            PostFilter.All -> visible
            PostFilter.Offer -> visible.filter { it.kind == PostKind.Offer }
            PostFilter.Ask -> visible.filter { it.kind == PostKind.Ask }
            PostFilter.Note -> visible.filter { it.kind == PostKind.Note }
        }
    }

    fun historyPosts(): List<HopPost> = history.toList()

    fun nearbyCount(): Int = 2

    fun addPost(
        kind: PostKind,
        title: String,
        body: String,
        profile: HopProfile,
    ): HopPost {
        val post = HopPost(
            id = "local-$nextId",
            kind = kind,
            title = title.trim(),
            body = body.trim(),
            authorName = profile.name.trim(),
            authorRoom = profile.room.trim(),
            authorId = SELF_ID,
            createdAtMillis = clock(),
        )
        nextId += 1
        floor.add(0, post)
        return post
    }

    fun claim(id: String): HopPost? {
        val index = floor.indexOfFirst { it.id == id }
        if (index < 0) return null
        val claimed = floor[index].copy(claimed = true)
        floor[index] = claimed
        if (history.none { it.id == id }) {
            history.add(0, claimed)
        }
        floor.removeAll { it.id == id }
        return claimed
    }

    fun remove(id: String, requesterId: String): Boolean {
        val post = floor.find { it.id == id } ?: history.find { it.id == id } ?: return false
        if (post.authorId != requesterId) return false
        floor.removeAll { it.id == id }
        history.removeAll { it.id == id }
        return true
    }

    fun isOwn(post: HopPost, requesterId: String = SELF_ID): Boolean = post.authorId == requesterId

    companion object {
        const val SELF_ID = "me"

        fun defaultSeed(now: Long = System.currentTimeMillis()): List<HopPost> = listOf(
            HopPost(
                id = "seed-1",
                kind = PostKind.Offer,
                title = "Spare rice cooker",
                body = "Clean, works. Borrow for tonight if your kitchen is packed.",
                authorName = "Priya",
                authorRoom = "204",
                authorId = "priya",
                createdAtMillis = now - 2 * 60_000,
            ),
            HopPost(
                id = "seed-2",
                kind = PostKind.Ask,
                title = "Phone charger",
                body = "USB-C brick overnight. Can swap a snack or leave a note.",
                authorName = "Mateo",
                authorRoom = "211",
                authorId = "mateo",
                createdAtMillis = now - 18 * 60_000,
            ),
            HopPost(
                id = "seed-3",
                kind = PostKind.Note,
                title = "Water tank",
                body = "Refill truck is at 8. Fill bottles before then.",
                authorName = "Anika",
                authorRoom = "201",
                authorId = "anika",
                createdAtMillis = now - 60 * 60_000,
            ),
            HopPost(
                id = "seed-4",
                kind = PostKind.Offer,
                title = "Extra blanket",
                body = "Folded on the landing chair. Take it if 218 is empty.",
                authorName = "Jonah",
                authorRoom = "218",
                authorId = "jonah",
                createdAtMillis = now - 3 * 60 * 60_000,
            ),
        )
    }
}

fun formatRelativeTime(createdAtMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
    val delta = (nowMillis - createdAtMillis).coerceAtLeast(0)
    val minutes = delta / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        else -> "${days}d"
    }
}

fun formatElapsed(seconds: Long): String {
    val safe = seconds.coerceAtLeast(0)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    val remain = safe % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, remain)
    } else {
        "%d:%02d".format(minutes, remain)
    }
}
