package com.flyme2mars.hop.ui.floor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.R
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.data.HopProfile
import com.flyme2mars.hop.data.NearbyAvailability
import com.flyme2mars.hop.data.NearbyState
import com.flyme2mars.hop.data.PostFilter
import com.flyme2mars.hop.data.PostKind
import com.flyme2mars.hop.ui.components.HopEmptyState
import com.flyme2mars.hop.ui.components.HopFilterChips
import com.flyme2mars.hop.ui.components.HopPostCard
import com.flyme2mars.hop.ui.theme.HopDimens
import com.flyme2mars.hop.ui.theme.HopTheme

@Composable
fun FloorScreen(
    profile: HopProfile,
    nearby: NearbyState,
    filter: PostFilter,
    posts: List<HopPost>,
    onFilter: (PostFilter) -> Unit,
    onOpenPost: (HopPost) -> Unit,
    onClaim: (HopPost) -> Unit,
    onNewPost: () -> Unit,
    onBlackout: () -> Unit,
    onRequestNearby: () -> Unit,
    onEnableBluetooth: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val colors = HopTheme.colors
    val subtitle = buildFloorSubtitle(profile, nearby)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = HopDimens.Side,
            end = HopDimens.Side,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Floor ${profile.floor.ifBlank { "?" }}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = onBlackout,
                        modifier = Modifier.size(HopDimens.Touch),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Bolt,
                            contentDescription = "Blackout",
                            tint = colors.textPrimary,
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
                if (nearby.availability == NearbyAvailability.Ready) {
                    Spacer(Modifier.height(8.dp))
                    if (nearby.peers.isEmpty()) {
                        Text(
                            text = "Nobody nearby",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textSecondary,
                        )
                    } else {
                        nearby.peers.forEach { peer ->
                            Text(
                                text = peer.label(),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (peer.name.isNotBlank()) {
                                    colors.textPrimary
                                } else {
                                    colors.textSecondary
                                },
                            )
                        }
                    }
                }
                if (nearby.needsPermission) {
                    TextButton(
                        onClick = onRequestNearby,
                        modifier = Modifier.heightIn(min = HopDimens.Touch),
                    ) {
                        Text(
                            text = stringResource(R.string.nearby_allow),
                            color = colors.accent,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Text(
                        text = stringResource(R.string.nearby_rationale),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                    )
                } else if (nearby.needsBluetooth) {
                    TextButton(
                        onClick = onEnableBluetooth,
                        modifier = Modifier.heightIn(min = HopDimens.Touch),
                    ) {
                        Text(
                            text = stringResource(R.string.nearby_enable_bluetooth),
                            color = colors.accent,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                HopFilterChips(selected = filter, onSelect = onFilter)
            }
        }
        if (posts.isEmpty()) {
            item {
                HopEmptyState(
                    title = "Nothing here yet",
                    actionLabel = "New post",
                    onAction = onNewPost,
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
        } else {
            items(posts, key = { it.id }) { post ->
                HopPostCard(
                    post = post,
                    onOpen = { onOpenPost(post) },
                    showClaim = post.kind != PostKind.Note,
                    onClaim = { onClaim(post) },
                )
            }
        }
    }
}

fun buildFloorSubtitle(profile: HopProfile, nearby: NearbyState): String {
    val parts = buildList {
        if (profile.room.isNotBlank()) add(profile.room)
        if (profile.name.isNotBlank()) add(profile.name)
        add(nearby.statusLine())
    }
    return parts.joinToString(" · ")
}
