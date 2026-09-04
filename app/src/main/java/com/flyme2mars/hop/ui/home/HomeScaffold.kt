package com.flyme2mars.hop.ui.home

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.flyme2mars.hop.HopAppState
import com.flyme2mars.hop.R
import com.flyme2mars.hop.data.HomeTab
import com.flyme2mars.hop.ui.components.HopInTreeSheet
import com.flyme2mars.hop.ui.floor.FloorScreen
import com.flyme2mars.hop.ui.history.HistoryScreen
import com.flyme2mars.hop.ui.post.ClaimPostContent
import com.flyme2mars.hop.ui.post.ComposePostContent
import com.flyme2mars.hop.ui.settings.SettingsScreen
import com.flyme2mars.hop.ui.theme.HopMotion
import com.flyme2mars.hop.ui.theme.HopSharedKeys
import com.flyme2mars.hop.ui.theme.HopTokens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScaffold(
    state: HopAppState,
    motion: HopMotion,
    sharedScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val showFab = state.tab == HomeTab.Floor &&
        !state.showPostComposer &&
        !state.showClaimSheet &&
        !state.showPostDetail
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    LaunchedEffect(route) {
        val tab = HomeTab.entries.firstOrNull { it.name == route }
        if (tab != null && tab != state.tab) {
            state.selectTab(tab)
        }
    }

    Box(modifier.fillMaxSize()) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = scheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tabLabel(state.tab),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                actions = {
                    if (state.tab == HomeTab.Floor) {
                        IconButton(
                            onClick = state::openCut,
                            modifier = Modifier.size(HopTokens.Touch),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = stringResource(R.string.cd_open_cut),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = scheme.surface,
                    scrolledContainerColor = scheme.surface,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = if (motion.reduced) {
                    fadeIn(animationSpec = motion.fade())
                } else {
                    fadeIn(animationSpec = motion.fade()) + scaleIn(animationSpec = motion.fabSpring())
                },
                exit = if (motion.reduced) {
                    fadeOut(animationSpec = motion.fade())
                } else {
                    fadeOut(animationSpec = motion.fade()) + scaleOut(animationSpec = motion.fabSpring())
                },
            ) {
                val fabAv = this
                with(sharedScope) {
                    FloatingActionButton(
                        onClick = state::openComposer,
                        modifier = Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState(HopSharedKeys.Composer),
                            animatedVisibilityScope = fabAv,
                        ),
                        containerColor = scheme.primary,
                        contentColor = scheme.onPrimary,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.cd_post),
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = scheme.surfaceContainer) {
                HomeTab.entries.forEach { tab ->
                    val selected = state.tab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (tab == state.tab) return@NavigationBarItem
                            state.selectTab(tab)
                            navController.navigate(tab.name) {
                                popUpTo(HomeTab.Floor.name) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tabIcon(tab, selected),
                                contentDescription = null,
                            )
                        },
                        label = {
                            Text(
                                text = tabLabel(tab),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeTab.Floor.name,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                directionalEnter(motion, initialState.destination.route, targetState.destination.route)
            },
            exitTransition = {
                directionalExit(motion, initialState.destination.route, targetState.destination.route)
            },
            popEnterTransition = {
                directionalEnter(motion, initialState.destination.route, targetState.destination.route)
            },
            popExitTransition = {
                directionalExit(motion, initialState.destination.route, targetState.destination.route)
            },
        ) {
            composable(HomeTab.Floor.name) {
                FloorScreen(
                    posts = state.posts,
                    filter = state.filter,
                    nearbyCount = state.nearbyCount,
                    motion = motion,
                    onFilterChange = { state.filter = it },
                    onOpenPost = state::openPost,
                    onCompose = state::openComposer,
                    contentPadding = innerPadding,
                    sharedScope = sharedScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            }
            composable(HomeTab.History.name) {
                HistoryScreen(
                    posts = state.history,
                    motion = motion,
                    onOpenPost = state::openPost,
                    contentPadding = innerPadding,
                    onEmptyCta = {
                        state.selectTab(HomeTab.Floor)
                        navController.navigate(HomeTab.Floor.name) {
                            popUpTo(HomeTab.Floor.name) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(HomeTab.Settings.name) {
                SettingsScreen(
                    nearbyCount = state.nearbyCount,
                    contentPadding = innerPadding,
                )
            }
        }
    }

        with(sharedScope) {
            HopInTreeSheet(
                visible = state.showPostComposer,
                onDismiss = state::dismissSheets,
                motion = motion,
                sheetModifier = {
                    Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(HopSharedKeys.Composer),
                        animatedVisibilityScope = this,
                    )
                },
            ) {
                ComposePostContent(
                    motion = motion,
                    onPublish = state::publishPost,
                )
            }

            val claim = state.selectedPost
            val showClaim = claim != null && (state.showClaimSheet || state.showPostDetail)
            HopInTreeSheet(
                visible = showClaim,
                onDismiss = state::dismissSheets,
                motion = motion,
                sheetModifier = {
                    if (claim == null) {
                        Modifier
                    } else {
                        Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState(HopSharedKeys.post(claim.id)),
                            animatedVisibilityScope = this,
                        )
                    }
                },
            ) { sheetAv ->
                if (claim != null) {
                    ClaimPostContent(
                        post = claim,
                        motion = motion,
                        sharedScope = sharedScope,
                        animatedVisibilityScope = sheetAv,
                        onDismiss = state::dismissSheets,
                        onConfirm = state::claimSelected,
                        onRemove = state::removeSelected,
                    )
                }
            }
        }
    }
}

private fun AnimatedContentTransitionScope<*>.directionalEnter(
    motion: HopMotion,
    from: String?,
    to: String?,
) = if (motion.reduced) {
    fadeIn(tween(HopTokens.ReducedMs))
} else {
    val forward = tabIndex(to) >= tabIndex(from)
    fadeIn(tween(HopTokens.MotionBase)) + slideIntoContainer(
        towards = if (forward) {
            AnimatedContentTransitionScope.SlideDirection.Start
        } else {
            AnimatedContentTransitionScope.SlideDirection.End
        },
        animationSpec = tween(HopTokens.MotionBase),
    )
}

private fun AnimatedContentTransitionScope<*>.directionalExit(
    motion: HopMotion,
    from: String?,
    to: String?,
) = if (motion.reduced) {
    fadeOut(tween(HopTokens.ReducedMs))
} else {
    val forward = tabIndex(to) >= tabIndex(from)
    fadeOut(tween(HopTokens.MotionFast)) + slideOutOfContainer(
        towards = if (forward) {
            AnimatedContentTransitionScope.SlideDirection.Start
        } else {
            AnimatedContentTransitionScope.SlideDirection.End
        },
        animationSpec = tween(HopTokens.MotionFast),
    )
}

private fun tabIndex(route: String?): Int =
    HomeTab.entries.indexOfFirst { it.name == route }.let { if (it < 0) 0 else it }

private fun tabIcon(tab: HomeTab, selected: Boolean) = when (tab) {
    HomeTab.Floor -> if (selected) Icons.Filled.Apartment else Icons.Outlined.Apartment
    HomeTab.History -> if (selected) Icons.Filled.History else Icons.Outlined.History
    HomeTab.Settings -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
}

@Composable
private fun tabLabel(tab: HomeTab): String = stringResource(
    when (tab) {
        HomeTab.Floor -> R.string.tab_floor
        HomeTab.History -> R.string.tab_history
        HomeTab.Settings -> R.string.tab_settings
    },
)
