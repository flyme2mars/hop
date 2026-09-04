package com.flyme2mars.hop.data

enum class PostKind {
    Request,
    Offer,
    Alert,
}

enum class PostFilter {
    All,
    Requests,
    Offers,
    Alerts,
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
    PostFilter.Requests -> kind == PostKind.Request
    PostFilter.Offers -> kind == PostKind.Offer
    PostFilter.Alerts -> kind == PostKind.Alert
}

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
