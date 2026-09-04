package com.flyme2mars.hop

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.flyme2mars.hop.data.HopRoute
import com.flyme2mars.hop.ui.cut.CutScreen
import com.flyme2mars.hop.ui.home.HomeScaffold
import com.flyme2mars.hop.ui.onboarding.OnboardingScreen
import com.flyme2mars.hop.ui.post.ClaimSheet
import com.flyme2mars.hop.ui.post.ComposePostSheet
import com.flyme2mars.hop.ui.post.PostDetailSheet
import com.flyme2mars.hop.ui.theme.HopTheme
import com.flyme2mars.hop.ui.theme.rememberHopMotion

@Composable
fun HopApp() {
    val context = LocalContext.current
    val state = remember { HopAppState(context) }
    val motion = rememberHopMotion()
    val cutMode = state.route is HopRoute.Cut

    HopTheme(cutMode = cutMode) {
        Crossfade(
            targetState = state.route,
            animationSpec = motion.cutEnter(),
            modifier = Modifier.fillMaxSize(),
            label = "hopRoute",
        ) { route ->
            when (route) {
                HopRoute.Onboarding -> OnboardingScreen(onGetStarted = state::completeOnboarding)
                HopRoute.Home -> HomeScaffold(state = state, motion = motion)
                HopRoute.Cut -> CutScreen(
                    nearbyCount = state.nearbyCount,
                    status = state.cutStatus,
                    motion = motion,
                    onOk = state::markCutOk,
                    onHelp = state::markCutHelp,
                    onLeave = state::leaveCut,
                )
            }
        }

        if (state.showPostComposer) {
            ComposePostSheet(
                onDismiss = state::dismissSheets,
                onPublish = state::publishPost,
            )
        }
        val detail = state.selectedPost
        if (state.showPostDetail && detail != null) {
            PostDetailSheet(
                post = detail,
                onDismiss = state::dismissSheets,
                onClaim = { state.openClaim(detail) },
                onRemove = state::removeSelected,
            )
        }
        if (state.showClaimSheet && detail != null) {
            ClaimSheet(
                post = detail,
                onDismiss = state::dismissSheets,
                onConfirm = state::claimSelected,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HopAppPreview() {
    HopApp()
}
