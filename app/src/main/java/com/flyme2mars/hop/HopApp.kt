package com.flyme2mars.hop

import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.flyme2mars.hop.data.nearby.NearbyPermissions
import com.flyme2mars.hop.ui.blackout.BlackoutScreen
import com.flyme2mars.hop.ui.components.HopGlassBottomBar
import com.flyme2mars.hop.ui.components.HopSideRail
import com.flyme2mars.hop.ui.floor.FloorScreen
import com.flyme2mars.hop.ui.history.HistoryScreen
import com.flyme2mars.hop.ui.launch.LaunchScreen
import com.flyme2mars.hop.ui.navigation.BlackoutRoute
import com.flyme2mars.hop.ui.navigation.HomeRoute
import com.flyme2mars.hop.ui.navigation.HomeTab
import com.flyme2mars.hop.ui.navigation.HopRoute
import com.flyme2mars.hop.ui.navigation.LaunchRoute
import com.flyme2mars.hop.ui.post.ClaimSheet
import com.flyme2mars.hop.ui.post.NewPostSheet
import com.flyme2mars.hop.ui.settings.SettingsScreen
import com.flyme2mars.hop.ui.theme.HopDimens
import com.flyme2mars.hop.ui.theme.HopTheme

@Composable
fun HopApp(viewModel: HopViewModel = viewModel()) {
    val start = if (viewModel.onboarded) HomeRoute else LaunchRoute
    val backStack = remember { mutableStateListOf<HopRoute>(start) }
    val current = backStack.lastOrNull() ?: LaunchRoute
    val activity = LocalActivity.current
    val holdScreen = current is BlackoutRoute && viewModel.keepScreenOn
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        viewModel.onNearbyPermissionsResult()
    }

    DisposableEffect(holdScreen, activity) {
        val window = activity?.window
        if (holdScreen) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(viewModel.onboarded) {
        if (viewModel.onboarded) {
            viewModel.startNearby()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopNearby() }
    }

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        },
        transitionSpec = {
            fadeIn(tween(220)) togetherWith fadeOut(tween(160))
        },
        popTransitionSpec = {
            fadeIn(tween(160)) togetherWith fadeOut(tween(140))
        },
        predictivePopTransitionSpec = {
            fadeIn(tween(160)) togetherWith fadeOut(tween(140))
        },
        entryProvider = { key ->
            when (key) {
                is LaunchRoute -> NavEntry(key) {
                    LaunchScreen(
                        profile = viewModel.profile,
                        onContinue = { name, room, floor ->
                            viewModel.completeLaunch(name, room, floor)
                            backStack.clear()
                            backStack.add(HomeRoute)
                        },
                    )
                }

                is HomeRoute -> NavEntry(key) {
                    HomeShell(
                        viewModel = viewModel,
                        onBlackout = {
                            viewModel.enterBlackout()
                            backStack.add(BlackoutRoute)
                        },
                        onRequestNearby = {
                            permissionLauncher.launch(NearbyPermissions.required())
                        },
                    )
                }

                is BlackoutRoute -> NavEntry(key) {
                    LaunchedEffect(Unit) { viewModel.enterBlackout() }
                    BlackoutScreen(
                        startedAtMillis = viewModel.blackoutStartedAt,
                        status = viewModel.blackoutStatus,
                        onStatus = viewModel::updateBlackoutStatus,
                        onClose = {
                            viewModel.exitBlackout()
                            if (backStack.lastOrNull() is BlackoutRoute) {
                                backStack.removeAt(backStack.lastIndex)
                            }
                        },
                    )
                }
            }
        },
    )
}

@Composable
private fun HomeShell(
    viewModel: HopViewModel,
    onBlackout: () -> Unit,
    onRequestNearby: () -> Unit,
) {
    val colors = HopTheme.colors
    var tab by rememberSaveable { mutableStateOf(HomeTab.Floor) }
    var showNewPost by rememberSaveable { mutableStateOf(false) }
    var openedPostId by rememberSaveable { mutableStateOf<String?>(null) }
    var openedFromHistory by rememberSaveable { mutableStateOf(false) }

    val openedPost = remember(openedPostId, viewModel.floorPosts, viewModel.historyPosts) {
        val id = openedPostId ?: return@remember null
        viewModel.floorPosts.find { it.id == id }
            ?: viewModel.historyPosts.find { it.id == id }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        val wide = maxWidth >= HopDimens.WideBreakpoint
        Row(modifier = Modifier.fillMaxSize()) {
            if (wide) {
                HopSideRail(selected = tab, onSelect = { tab = it })
            }
            Scaffold(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                containerColor = colors.bg,
                floatingActionButton = {
                    if (tab == HomeTab.Floor) {
                        FloatingActionButton(
                            onClick = { showNewPost = true },
                            containerColor = colors.accent,
                            contentColor = colors.accentText,
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "New post")
                        }
                    }
                },
                bottomBar = {
                    if (!wide) {
                        HopGlassBottomBar(selected = tab, onSelect = { tab = it })
                    }
                },
            ) { innerPadding ->
                AnimatedContent(
                    targetState = tab,
                    transitionSpec = {
                        fadeIn(tween(180)) togetherWith fadeOut(tween(140))
                    },
                    label = "home-tab",
                ) { current ->
                    when (current) {
                        HomeTab.Floor -> FloorScreen(
                            profile = viewModel.profile,
                            nearby = viewModel.nearbyState,
                            filter = viewModel.filter,
                            posts = viewModel.floorPosts,
                            onFilter = viewModel::updateFilter,
                            onOpenPost = { post ->
                                openedFromHistory = false
                                openedPostId = post.id
                            },
                            onClaim = { post ->
                                openedFromHistory = false
                                openedPostId = post.id
                            },
                            onNewPost = { showNewPost = true },
                            onBlackout = onBlackout,
                            onRequestNearby = onRequestNearby,
                            contentPadding = innerPadding,
                        )

                        HomeTab.History -> HistoryScreen(
                            posts = viewModel.historyPosts,
                            onOpenPost = { post ->
                                openedFromHistory = true
                                openedPostId = post.id
                            },
                            contentPadding = innerPadding,
                        )

                        HomeTab.Settings -> SettingsScreen(
                            profile = viewModel.profile,
                            keepScreenOn = viewModel.keepScreenOn,
                            onSaveProfile = viewModel::updateProfile,
                            onKeepScreenOnChange = viewModel::updateKeepScreenOn,
                            contentPadding = innerPadding,
                        )
                    }
                }
            }
        }
    }

    if (showNewPost) {
        NewPostSheet(
            onDismiss = { showNewPost = false },
            onPost = viewModel::addPost,
        )
    }

    openedPost?.let { post ->
        ClaimSheet(
            post = post,
            isOwn = viewModel.isOwn(post),
            quieter = openedFromHistory,
            onDismiss = { openedPostId = null },
            onClaim = { viewModel.claim(post) },
            onRemove = { viewModel.remove(post) },
        )
    }
}
