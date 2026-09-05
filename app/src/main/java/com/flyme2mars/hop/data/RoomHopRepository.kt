package com.flyme2mars.hop.data

import com.flyme2mars.hop.data.db.PostDao
import com.flyme2mars.hop.data.db.toEntity
import com.flyme2mars.hop.data.db.toModel
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomHopRepository(
    private val dao: PostDao,
    private val settings: HopSettingsStore,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val idFactory: () -> String = { "hop-${UUID.randomUUID()}" },
) {
    fun observePosts(): Flow<List<HopPost>> = dao.observeAll().map { rows ->
        rows.mapNotNull { it.toModel() }
    }

    suspend fun ensureSeeded() {
        val snapshot = settings.snapshot()
        if (snapshot.seeded) return
        if (dao.count() == 0) {
            defaultSeedPosts(clock()).forEach { dao.insertIgnore(it.toEntity()) }
        }
        settings.markSeeded()
    }

    suspend fun addPost(
        kind: PostKind,
        title: String,
        body: String,
        profile: HopProfile,
        authorId: String,
    ): HopPost {
        val post = HopPost(
            id = idFactory(),
            kind = kind,
            title = title.trim(),
            body = body.trim(),
            authorName = profile.name.trim(),
            authorRoom = profile.room.trim(),
            authorId = authorId,
            createdAtMillis = clock(),
        )
        dao.upsert(post.toEntity())
        return post
    }

    suspend fun claim(id: String): HopPost? {
        val existing = dao.getById(id)?.toModel() ?: return null
        dao.markClaimed(id)
        return existing.copy(claimed = true)
    }

    suspend fun remove(id: String, requesterId: String): Boolean {
        val existing = dao.getById(id)?.toModel() ?: return false
        if (existing.authorId != requesterId) return false
        dao.delete(id)
        return true
    }

    suspend fun ingestRemote(remotePosts: List<HopPost>) {
        remotePosts.forEach { remote ->
            val local = dao.getById(remote.id)?.toModel()
            val merged = mergeRemotePost(local, remote)
            if (local == null) {
                dao.insertIgnore(merged.toEntity())
            } else if (merged.claimed && !local.claimed) {
                dao.markClaimed(local.id)
            }
        }
    }

    suspend fun snapshotForSync(): List<HopPost> = dao.getAll().mapNotNull { it.toModel() }

    fun isOwn(post: HopPost, requesterId: String): Boolean = post.authorId == requesterId
}
