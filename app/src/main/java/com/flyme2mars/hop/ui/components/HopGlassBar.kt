package com.flyme2mars.hop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.ui.navigation.HomeTab
import com.flyme2mars.hop.ui.theme.HopDimens
import com.flyme2mars.hop.ui.theme.HopTheme

data class HopNavItem(
    val tab: HomeTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val HopNavItems = listOf(
    HopNavItem(HomeTab.Floor, "Floor", Icons.Filled.Home, Icons.Outlined.Home),
    HopNavItem(HomeTab.History, "History", Icons.Filled.History, Icons.Outlined.History),
    HopNavItem(HomeTab.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
fun HopGlassBottomBar(
    selected: HomeTab,
    onSelect: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HopTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.glassFill.copy(alpha = 0.36f),
                        colors.glassFill,
                    ),
                ),
            )
            .background(colors.glassFill)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.glassEdge),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(HopDimens.BottomBar)
                .selectableGroup()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HopNavItems.forEach { item ->
                HopBarItem(
                    item = item,
                    selected = item.tab == selected,
                    onSelect = { onSelect(item.tab) },
                )
            }
        }
    }
}

@Composable
private fun RowScope.HopBarItem(
    item: HopNavItem,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val colors = HopTheme.colors
    val tint = if (selected) colors.accent else colors.textSecondary
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.Tab,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelMedium,
            color = tint,
        )
    }
}

@Composable
fun HopSideRail(
    selected: HomeTab,
    onSelect: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HopTheme.colors
    NavigationRail(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = colors.textSecondary,
    ) {
        HopNavItems.forEach { item ->
            NavigationRailItem(
                selected = item.tab == selected,
                onClick = { onSelect(item.tab) },
                icon = {
                    Icon(
                        imageVector = if (item.tab == selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = colors.accent,
                    selectedTextColor = colors.accent,
                    unselectedIconColor = colors.textSecondary,
                    unselectedTextColor = colors.textSecondary,
                    indicatorColor = colors.surfaceRaised,
                ),
            )
        }
    }
}
