package com.flyme2mars.hop.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.flyme2mars.hop.R
import com.flyme2mars.hop.data.PostFilter
import com.flyme2mars.hop.ui.theme.HopMotion
import com.flyme2mars.hop.ui.theme.HopTokens

@Composable
fun HopSegmentedFilter(
    selected: PostFilter,
    onSelect: (PostFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brushAlpha = if (dark) HopTokens.SegmentBrushDark else HopTokens.SegmentBrushLight
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HopTokens.Radius16))
            .background(scheme.surfaceContainer)
            .padding(HopTokens.Radius4),
    ) {
        PostFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = HopTokens.Touch)
                    .clip(RoundedCornerShape(HopTokens.Radius12))
                    .background(if (isSelected) scheme.surfaceContainerHigh else Color.Transparent)
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                Brush.verticalGradient(
                                    listOf(
                                        scheme.primary.copy(alpha = brushAlpha),
                                        Color.Transparent,
                                    ),
                                ),
                            )
                        } else {
                            Modifier
                        },
                    )
                    .clickable(role = Role.Tab) { onSelect(filter) }
                    .padding(vertical = HopTokens.Radius8),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = filter.label(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) scheme.onSurface else scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun PostFilter.label(): String = stringResource(
    when (this) {
        PostFilter.All -> R.string.filter_all
        PostFilter.Offers -> R.string.filter_offers
        PostFilter.Asks -> R.string.filter_asks
        PostFilter.Notes -> R.string.filter_notes
    },
)

@Composable
fun HopFilterPages(
    filter: PostFilter,
    motion: HopMotion,
    modifier: Modifier = Modifier,
    content: @Composable (PostFilter) -> Unit,
) {
    AnimatedContent(
        targetState = filter,
        modifier = modifier,
        transitionSpec = {
            val from = PostFilter.entries.indexOf(initialState)
            val to = PostFilter.entries.indexOf(targetState)
            val forward = to >= from
            if (motion.reduced) {
                fadeIn(tween(HopTokens.ReducedMs)) togetherWith fadeOut(tween(HopTokens.ReducedMs))
            } else {
                (
                    fadeIn(tween(HopTokens.MotionBase)) +
                        slideInHorizontally { if (forward) it / 6 else -it / 6 }
                    ) togetherWith (
                    fadeOut(tween(HopTokens.MotionFast)) +
                        slideOutHorizontally { if (forward) -it / 6 else it / 6 }
                    )
            }
        },
        label = "filterPages",
    ) { page ->
        content(page)
    }
}
