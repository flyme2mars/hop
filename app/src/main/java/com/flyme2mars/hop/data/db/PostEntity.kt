package com.flyme2mars.hop.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.data.PostKind

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val kind: String,
    val title: String,
    val body: String,
    val authorName: String,
    val authorRoom: String,
    val authorId: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val claimed: Boolean,
)

fun PostEntity.toModel(): HopPost? {
    val parsedKind = runCatching { PostKind.valueOf(kind) }.getOrNull() ?: return null
    return HopPost(
        id = id,
        kind = parsedKind,
        title = title,
        body = body,
        authorName = authorName,
        authorRoom = authorRoom,
        authorId = authorId,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = if (updatedAtMillis > 0L) updatedAtMillis else createdAtMillis,
        claimed = claimed,
    )
}

fun HopPost.toEntity(): PostEntity = PostEntity(
    id = id,
    kind = kind.name,
    title = title,
    body = body,
    authorName = authorName,
    authorRoom = authorRoom,
    authorId = authorId,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = if (updatedAtMillis > 0L) updatedAtMillis else createdAtMillis,
    claimed = claimed,
)
