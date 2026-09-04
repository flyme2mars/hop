package com.flyme2mars.hop.ui.floor

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.flyme2mars.hop.R
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.data.PostFilter
import com.flyme2mars.hop.data.matches
import com.flyme2mars.hop.ui.components.HopEmptyState
import com.flyme2mars.hop.ui.components.HopFilterPages
import com.flyme2mars.hop.ui.components.HopSegmentedFilter
import com.flyme2mars.hop.ui.theme.HopMotion
import com.flyme2mars.hop.ui.theme.HopSharedKeys
import com.flyme2mars.hop.ui.theme.HopTokens

@OptIn(ExperimentalSharedTransitionApi::class)
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
    sharedScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val primary = scheme.primary

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBehind { drawFloorVignette(dark, primary) }
            .padding(contentPadding),
    ) {
        Text(
            text = pluralStringResource(R.plurals.nearby_count, nearbyCount, nearbyCount),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = HopTokens.ScreenGutter,
                    end = HopTokens.ScreenGutter,
                    top = HopTokens.ListGap,
                    bottom = HopTokens.Radius8,
                ),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurfaceVariant,
        )
        HopSegmentedFilter(
            selected = filter,
            onSelect = onFilterChange,
            modifier = Modifier.padding(horizontal = HopTokens.ScreenGutter),
        )
        HopFilterPages(
            filter = filter,
            motion = motion,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { page ->
            val filtered = remember(posts, page) { posts.filter { it.matches(page) } }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = HopTokens.ScreenGutter,
                    end = HopTokens.ScreenGutter,
                    top = HopTokens.ListGap,
                    bottom = HopTokens.SectionGap,
                ),
                verticalArrangement = Arrangement.spacedBy(HopTokens.ListGap),
            ) {
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
                        with(sharedScope) {
                            PostCard(
                                post = post,
                                onClick = { onOpenPost(post) },
                                modifier = Modifier.sharedBounds(
                                    sharedContentState = rememberSharedContentState(
                                        key = HopSharedKeys.post(post.id),
                                    ),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                ),
                                titleModifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState(
                                        key = HopSharedKeys.title(post.id),
                                    ),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                ),
                                roomModifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState(
                                        key = HopSharedKeys.room(post.id),
                                    ),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                ),
                                statusModifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState(
                                        key = HopSharedKeys.status(post.id),
                                    ),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFloorVignette(
    dark: Boolean,
    primary: Color,
) {
    val edge = if (dark) HopTokens.VignetteDark else HopTokens.VignetteLight
    drawRect(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.42f to Color.Transparent,
                1f to Color.Black.copy(alpha = edge),
            ),
            center = Offset(size.width * 0.5f, size.height * 0.42f),
            radius = size.maxDimension * 0.78f,
        ),
    )
    if (dark) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.04f), Color.Transparent),
                center = Offset(size.width * 0.16f, size.height * 0.06f),
                radius = size.minDimension * 0.55f,
            ),
        )
    }
}
