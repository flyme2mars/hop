package com.flyme2mars.hop.data

enum class PostKind {
    Ask,
    Offer,
    Note,
}

val PostKind.hasPrioritySheen: Boolean
    get() = this == PostKind.Ask || this == PostKind.Offer

enum class PostFilter {
    All,
    Asks,
    Offers,
    Notes,
}

data class HopPost(
    val id: Long,
    val kind: PostKind,
    val title: String,
    val body: String,
    val author: String,
    val place: String,
    val postedAgo: String,
    val claimedBy: String? = null,
)

fun HopPost.matches(filter: PostFilter): Boolean = when (filter) {
    PostFilter.All -> true
    PostFilter.Asks -> kind == PostKind.Ask
    PostFilter.Offers -> kind == PostKind.Offer
    PostFilter.Notes -> kind == PostKind.Note
}

fun HopPost.canClaim(): Boolean = kind == PostKind.Ask && claimedBy == null

enum class HomeTab {
    Floor,
    History,
    Settings,
}

sealed interface HopRoute {
    data object Onboarding : HopRoute
    data object Home : HopRoute
    data object Cut : HopRoute
}

fun authorInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (parts.isEmpty()) return "?"
    return if (parts.size == 1) {
        parts[0].take(2).uppercase()
    } else {
        "${parts[0].first()}${parts[1].first()}".uppercase()
    }
}
