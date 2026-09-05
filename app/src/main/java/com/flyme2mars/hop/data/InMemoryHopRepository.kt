package com.flyme2mars.hop.data

class InMemoryHopRepository(
    seed: List<HopPost> = defaultSeedPosts(),
    alreadySeeded: Boolean = false,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val idFactory: () -> String = { "local-${clock()}" },
) {
    private val posts = seed.toMutableList()
    private var seeded = alreadySeeded
    var selfId: String = SELF_ID

    fun ensureSeeded(defaults: List<HopPost> = defaultSeedPosts(clock())) {
        if (seeded) return
        if (posts.isEmpty()) {
            posts.addAll(defaults)
        }
        seeded = true
    }

    fun isSeeded(): Boolean = seeded

    fun floorPosts(filter: PostFilter = PostFilter.All): List<HopPost> = posts.visibleFloor(filter)

    fun historyPosts(): List<HopPost> = posts.claimedHistory()

    fun allPosts(): List<HopPost> = posts.toList()

    fun addPost(
        kind: PostKind,
        title: String,
        body: String,
        profile: HopProfile,
        authorId: String = selfId,
    ): HopPost {
        val now = clock()
        val post = HopPost(
            id = idFactory(),
            kind = kind,
            title = title.trim(),
            body = body.trim(),
            authorName = profile.name.trim(),
            authorRoom = profile.room.trim(),
            authorId = authorId,
            createdAtMillis = now,
            updatedAtMillis = now,
        )
        posts.add(0, post)
        return post
    }

    fun claim(id: String): HopPost? {
        val index = posts.indexOfFirst { it.id == id }
        if (index < 0) return null
        val claimed = posts[index].copy(claimed = true, updatedAtMillis = clock())
        posts[index] = claimed
        return claimed
    }

    fun remove(id: String, requesterId: String): Boolean {
        val post = posts.find { it.id == id } ?: return false
        if (post.authorId != requesterId) return false
        posts.removeAll { it.id == id }
        return true
    }

    fun ingestRemote(remotePosts: List<HopPost>) {
        remotePosts.forEach { remote ->
            val index = posts.indexOfFirst { it.id == remote.id }
            if (index < 0) {
                posts.add(remote)
            } else {
                posts[index] = mergeRemotePost(posts[index], remote)
            }
        }
    }

    fun isOwn(post: HopPost, requesterId: String = selfId): Boolean = post.authorId == requesterId

    companion object {
        const val SELF_ID = "me"
    }
}
