package com.flyme2mars.hop.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flyme2mars.hop.HopAppState
import com.flyme2mars.hop.R
import com.flyme2mars.hop.data.HomeTab
import com.flyme2mars.hop.ui.floor.FloorScreen
import com.flyme2mars.hop.ui.history.HistoryScreen
import com.flyme2mars.hop.ui.settings.SettingsScreen
import com.flyme2mars.hop.ui.theme.HopMotion

@Composable
fun HomeScaffold(
    state: HopAppState,
    motion: HopMotion,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val showFab = state.tab == HomeTab.Floor && !state.showPostComposer

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = scheme.surface,
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
                ExtendedFloatingActionButton(
                    onClick = state::openComposer,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.fab_post),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary,
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = scheme.surfaceContainer) {
                HomeTab.entries.forEach { tab ->
                    val selected = state.tab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { state.selectTab(tab) },
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
        when (state.tab) {
            HomeTab.Floor -> FloorScreen(
                posts = state.posts,
                filter = state.filter,
                nearbyCount = state.nearbyCount,
                motion = motion,
                onFilterChange = { state.filter = it },
                onOpenPost = state::openPost,
                onOpenCut = state::openCut,
                contentPadding = innerPadding,
            )
            HomeTab.History -> HistoryScreen(
                posts = state.history,
                motion = motion,
                onOpenPost = state::openPost,
                contentPadding = innerPadding,
            )
            HomeTab.Settings -> SettingsScreen(
                nearbyCount = state.nearbyCount,
                contentPadding = innerPadding,
            )
        }
    }
}

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
