package com.flyme2mars.hop.data

enum class PostKind {
    Offer,
    Ask,
    Note,
}

enum class PostFilter {
    All,
    Offer,
    Ask,
    Note,
}

data class HopProfile(
    val name: String = "",
    val room: String = "",
    val floor: String = "",
)

data class HopPost(
    val id: String,
    val kind: PostKind,
    val title: String,
    val body: String,
    val authorName: String,
    val authorRoom: String,
    val authorId: String,
    val createdAtMillis: Long,
    val claimed: Boolean = false,
)

data class HopPrefs(
    val profile: HopProfile = HopProfile(),
    val onboarded: Boolean = false,
    val keepScreenOn: Boolean = true,
)
