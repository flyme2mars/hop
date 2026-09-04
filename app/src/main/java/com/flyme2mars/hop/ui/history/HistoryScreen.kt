package com.flyme2mars.hop.ui.history

import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flyme2mars.hop.R
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.ui.components.HopEmptyState
import com.flyme2mars.hop.ui.floor.PostCard
import com.flyme2mars.hop.ui.theme.HopMotion
import com.flyme2mars.hop.ui.theme.HopTokens

@Composable
fun HistoryScreen(
    posts: List<HopPost>,
    motion: HopMotion,
    onOpenPost: (HopPost) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onEmptyCta: () -> Unit = {},
) {
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
        if (posts.isEmpty()) {
            item(key = "history_empty") {
                HopEmptyState(
                    title = stringResource(R.string.history_empty_title),
                    body = stringResource(R.string.history_empty_body),
                    cta = stringResource(R.string.history_empty_cta),
                    onCta = onEmptyCta,
                )
            }
        } else {
            items(items = posts, key = { it.id }) { post ->
                val itemModifier = if (motion.reduced) {
                    Modifier.animateItem(fadeInSpec = snap(), fadeOutSpec = snap(), placementSpec = snap())
                } else {
                    Modifier.animateItem(placementSpec = motion.placement())
                }
                PostCard(
                    post = post,
                    onClick = { onOpenPost(post) },
                    modifier = itemModifier,
                    muted = true,
                )
            }
        }
    }
}
