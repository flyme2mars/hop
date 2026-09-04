package com.flyme2mars.hop.ui.floor

import androidx.compose.animation.core.snap
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.FrontHand
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.R
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.data.PostFilter
import com.flyme2mars.hop.data.matches
import com.flyme2mars.hop.ui.components.HopAssistChip
import com.flyme2mars.hop.ui.components.HopFilterChip
import com.flyme2mars.hop.ui.components.HopScreenHeader
import com.flyme2mars.hop.ui.theme.HopMotion

@Composable
fun FloorScreen(
    posts: List<HopPost>,
    filter: PostFilter,
    nearbyCount: Int,
    motion: HopMotion,
    onFilterChange: (PostFilter) -> Unit,
    onOpenPost: (HopPost) -> Unit,
    onOpenCut: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val filtered = remember(posts, filter) { posts.filter { it.matches(filter) } }
    val scheme = MaterialTheme.colorScheme

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(HopCardGap),
    ) {
        item(key = "floor_header") {
            HopScreenHeader(
                title = stringResource(R.string.floor_title),
                subtitle = {
                    HopAssistChip(
                        label = pluralStringResource(R.plurals.nearby_count, nearbyCount, nearbyCount),
                        icon = Icons.Outlined.Sensors,
                    )
                },
                actions = {
                    IconButton(onClick = onOpenCut) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = stringResource(R.string.cd_open_cut),
                            tint = scheme.onSurface,
                        )
                    }
                },
            )
        }
        item(key = "floor_filters") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HopFilterChip(
                    selected = filter == PostFilter.All,
                    onClick = { onFilterChange(PostFilter.All) },
                    label = stringResource(R.string.filter_all),
                    icon = Icons.Outlined.Layers,
                )
                HopFilterChip(
                    selected = filter == PostFilter.Requests,
                    onClick = { onFilterChange(PostFilter.Requests) },
                    label = stringResource(R.string.filter_requests),
                    icon = Icons.Outlined.FrontHand,
                )
                HopFilterChip(
                    selected = filter == PostFilter.Offers,
                    onClick = { onFilterChange(PostFilter.Offers) },
                    label = stringResource(R.string.filter_offers),
                    icon = Icons.Outlined.Redeem,
                )
                HopFilterChip(
                    selected = filter == PostFilter.Alerts,
                    onClick = { onFilterChange(PostFilter.Alerts) },
                    label = stringResource(R.string.filter_alerts),
                    icon = Icons.Outlined.Campaign,
                )
            }
        }
        if (filtered.isEmpty()) {
            item(key = "floor_empty") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = HopCardShape,
                    color = scheme.surfaceContainer,
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.floor_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = scheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.floor_empty_body),
                            style = MaterialTheme.typography.bodyLarge,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
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
