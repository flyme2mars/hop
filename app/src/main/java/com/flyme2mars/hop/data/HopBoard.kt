package com.flyme2mars.hop.data

fun List<HopPost>.visibleFloor(filter: PostFilter): List<HopPost> {
    val visible = filterNot { it.claimed }.sortedByDescending { it.createdAtMillis }
    return when (filter) {
        PostFilter.All -> visible
        PostFilter.Offer -> visible.filter { it.kind == PostKind.Offer }
        PostFilter.Ask -> visible.filter { it.kind == PostKind.Ask }
        PostFilter.Note -> visible.filter { it.kind == PostKind.Note }
    }
}

fun List<HopPost>.claimedHistory(): List<HopPost> =
    filter { it.claimed }.sortedByDescending { it.createdAtMillis }

fun mergeRemotePost(local: HopPost?, remote: HopPost): HopPost {
    if (local == null) return remote
    return when {
        remote.updatedAtMillis > local.updatedAtMillis -> remote
        remote.updatedAtMillis < local.updatedAtMillis -> local
        else -> local.copy(claimed = local.claimed || remote.claimed)
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

fun defaultSeedPosts(now: Long = System.currentTimeMillis()): List<HopPost> = listOf(
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
