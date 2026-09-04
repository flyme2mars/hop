package com.flyme2mars.hop

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.core.content.edit
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.flyme2mars.hop.data.FakeHopRepository
import com.flyme2mars.hop.data.HomeTab
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.data.HopRoute
import com.flyme2mars.hop.data.PostFilter
import com.flyme2mars.hop.data.PostKind

class HopAppState(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var route: HopRoute by mutableStateOf(
        if (prefs.getBoolean(KEY_ONBOARDED, false)) HopRoute.Home else HopRoute.Onboarding,
    )
        private set

    var tab: HomeTab by mutableStateOf(HomeTab.Floor)
        private set

    var filter: PostFilter by mutableStateOf(PostFilter.All)

    var posts: List<HopPost> by mutableStateOf(FakeHopRepository.seedPosts())
        private set

    val history: List<HopPost> = FakeHopRepository.seedHistory()

    val nearbyCount: Int = FakeHopRepository.NearbyCount

    var selectedPost: HopPost? by mutableStateOf(null)
        private set

    var showPostComposer: Boolean by mutableStateOf(false)
        private set

    var showPostDetail: Boolean by mutableStateOf(false)
        private set

    var showClaimSheet: Boolean by mutableStateOf(false)
        private set

    var cutStatus: String? by mutableStateOf(null)
        private set

    private var nextId by mutableLongStateOf(200L)

    fun completeOnboarding() {
        prefs.edit { putBoolean(KEY_ONBOARDED, true) }
        route = HopRoute.Home
        tab = HomeTab.Floor
    }

    fun selectTab(homeTab: HomeTab) {
        tab = homeTab
        dismissSheets()
    }

    fun openCut() {
        dismissSheets()
        cutStatus = null
        route = HopRoute.Cut
    }

    fun leaveCut() {
        route = HopRoute.Home
        tab = HomeTab.Floor
    }

    fun openComposer() {
        showPostDetail = false
        showClaimSheet = false
        showPostComposer = true
    }

    fun openPost(post: HopPost) {
        selectedPost = post
        showPostComposer = false
        showClaimSheet = false
        showPostDetail = true
    }

    fun openClaim(post: HopPost) {
        selectedPost = post
        showPostComposer = false
        showPostDetail = false
        showClaimSheet = true
    }

    fun dismissSheets() {
        showPostComposer = false
        showPostDetail = false
        showClaimSheet = false
    }

    fun publishPost(kind: PostKind, title: String, body: String) {
        val trimmedTitle = title.trim()
        val trimmedBody = body.trim()
        if (trimmedTitle.isEmpty() || trimmedBody.isEmpty()) return
        val post = HopPost(
            id = nextId,
            kind = kind,
            title = trimmedTitle,
            body = trimmedBody,
            author = FakeHopRepository.YouName,
            place = "This floor",
            postedAgo = "Just now",
        )
        nextId += 1
        posts = listOf(post) + posts
        dismissSheets()
        tab = HomeTab.Floor
        filter = PostFilter.All
    }

    fun claimSelected() {
        val post = selectedPost ?: return
        posts = posts.map { existing ->
            if (existing.id == post.id) {
                existing.copy(claimedBy = FakeHopRepository.YouName)
            } else {
                existing
            }
        }
        selectedPost = posts.firstOrNull { it.id == post.id }
        dismissSheets()
    }

    fun removeSelected() {
        val post = selectedPost ?: return
        posts = posts.map { existing ->
            if (existing.id == post.id) existing.copy(claimedBy = null) else existing
        }
        selectedPost = posts.firstOrNull { it.id == post.id }
        dismissSheets()
    }

    fun markCutOk() {
        cutStatus = CUT_OK
    }

    fun markCutHelp() {
        cutStatus = CUT_HELP
    }

    companion object {
        private const val PREFS_NAME = "hop"
        private const val KEY_ONBOARDED = "onboarded"
        const val CUT_OK = "ok"
        const val CUT_HELP = "help"
    }
}
