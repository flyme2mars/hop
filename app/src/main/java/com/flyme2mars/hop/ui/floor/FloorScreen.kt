package com.flyme2mars.hop.ui.floor

import androidx.compose.animation.core.snap
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FrontHand
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.flyme2mars.hop.R
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.data.PostFilter
import com.flyme2mars.hop.data.matches
import com.flyme2mars.hop.ui.components.HopAssistChip
import com.flyme2mars.hop.ui.components.HopEmptyState
import com.flyme2mars.hop.ui.components.HopFilterChip
import com.flyme2mars.hop.ui.theme.HopMotion
import com.flyme2mars.hop.ui.theme.HopTokens

@Composable
fun FloorScreen(
    posts: List<HopPost>,
    filter: PostFilter,
    nearbyCount: Int,
    motion: HopMotion,
    onFilterChange: (PostFilter) -> Unit,
    onOpenPost: (HopPost) -> Unit,
    onCompose: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val filtered = remember(posts, filter) { posts.filter { it.matches(filter) } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = HopTokens.ScreenGutter,
            end = HopTokens.ScreenGutter,
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + HopTokens.SectionGap,
        ),
        verticalArrangement = Arrangement.spacedBy(HopTokens.ListGap),
    ) {
        item(key = "floor_nearby") {
            HopAssistChip(
                label = pluralStringResource(R.plurals.nearby_count, nearbyCount, nearbyCount),
                icon = Icons.Outlined.Sensors,
            )
        }
        item(key = "floor_filters") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(HopTokens.Radius8),
            ) {
                HopFilterChip(
                    selected = filter == PostFilter.All,
                    onClick = { onFilterChange(PostFilter.All) },
                    label = stringResource(R.string.filter_all),
                    icon = Icons.Outlined.Layers,
                )
                HopFilterChip(
                    selected = filter == PostFilter.Asks,
                    onClick = { onFilterChange(PostFilter.Asks) },
                    label = stringResource(R.string.filter_asks),
                    icon = Icons.Outlined.FrontHand,
                )
                HopFilterChip(
                    selected = filter == PostFilter.Offers,
                    onClick = { onFilterChange(PostFilter.Offers) },
                    label = stringResource(R.string.filter_offers),
                    icon = Icons.Outlined.Redeem,
                )
                HopFilterChip(
                    selected = filter == PostFilter.Notes,
                    onClick = { onFilterChange(PostFilter.Notes) },
                    label = stringResource(R.string.filter_notes),
                    icon = Icons.AutoMirrored.Outlined.Notes,
                )
            }
        }
        if (filtered.isEmpty()) {
            item(key = "floor_empty") {
                HopEmptyState(
                    title = stringResource(R.string.floor_empty_title),
                    body = stringResource(R.string.floor_empty_body),
                    cta = stringResource(R.string.floor_empty_cta),
                    onCta = onCompose,
                )
            }
        } else {
            items(items = filtered, key = { it.id }) { post ->
                val itemModifier = if (motion.reduced) {
                    Modifier.animateItem(fadeInSpec = snap(), fadeOutSpec = snap(), placementSpec = snap())
                } else {
                    Modifier.animateItem(placementSpec = motion.placement())
                }
                PostCard(
                    post = post,
                    onClick = { onOpenPost(post) },
                    modifier = itemModifier,
                )
            }
        }
    }
}
