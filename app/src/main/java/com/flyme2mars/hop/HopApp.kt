package com.flyme2mars.hop

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import com.flyme2mars.hop.data.HopRoute
import com.flyme2mars.hop.ui.cut.CutScreen
import com.flyme2mars.hop.ui.home.HomeScaffold
import com.flyme2mars.hop.ui.onboarding.OnboardingScreen
import com.flyme2mars.hop.ui.theme.HopTheme
import com.flyme2mars.hop.ui.theme.HopTokens
import com.flyme2mars.hop.ui.theme.rememberHopMotion

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HopApp() {
    val context = LocalContext.current
    val state = remember { HopAppState(context) }
    val motion = rememberHopMotion()
    val onHome = state.route != HopRoute.Onboarding
    val showCut = state.route is HopRoute.Cut
    val reduced = motion.reduced
    val cutSlidePx = with(LocalDensity.current) { HopTokens.CutSlide.roundToPx() }

    HopTheme(cutMode = showCut) {
        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
            val sharedScope = this
            AnimatedContent(
                targetState = onHome,
                transitionSpec = {
                    if (reduced) {
                        fadeIn(tween(HopTokens.ReducedMs)) togetherWith
                            fadeOut(tween(HopTokens.ReducedMs))
                    } else {
                        fadeIn(tween(HopTokens.MotionBase)) +
                            slideInVertically { it / 18 } togetherWith
                            fadeOut(tween(HopTokens.MotionBase))
                    }
                },
                label = "hop-stage",
                modifier = Modifier.fillMaxSize(),
            ) { home ->
                if (!home) {
                    OnboardingScreen(
                        motion = motion,
                        onJoin = state::completeOnboarding,
                    )
                } else {
                    Box(Modifier.fillMaxSize()) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(0)),
                            exit = fadeOut(tween(0)),
                            label = "floor-always-mounted",
                        ) {
                            val floorAv = this
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        if (showCut && !reduced) {
                                            scaleX = HopTokens.FloorCutScale
                                            scaleY = HopTokens.FloorCutScale
                                            alpha = 0.72f
                                        } else if (showCut) {
                                            alpha = 0.72f
                                        }
                                    },
                            ) {
                                HomeScaffold(
                                    state = state,
                                    motion = motion,
                                    sharedScope = sharedScope,
                                    animatedVisibilityScope = floorAv,
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = showCut,
                            enter = if (reduced) {
                                fadeIn(tween(HopTokens.ReducedMs))
                            } else {
                                fadeIn(tween(HopTokens.CutEnterMs)) +
                                    slideInVertically { cutSlidePx }
                            },
                            exit = if (reduced) {
                                fadeOut(tween(HopTokens.ReducedMs))
                            } else {
                                fadeOut(tween(HopTokens.CutEnterMs)) +
                                    slideOutVertically { cutSlidePx }
                            },
                            label = "cut-overlay",
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            CutScreen(
                                nearbyCount = state.nearbyCount,
                                status = state.cutStatus,
                                motion = motion,
                                onOk = state::markCutOk,
                                onHelp = state::markCutHelp,
                                onLeave = state::leaveCut,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HopAppPreview() {
    HopApp()
}
