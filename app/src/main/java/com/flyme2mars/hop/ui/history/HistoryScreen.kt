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
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.R
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.ui.components.HopScreenHeader
import com.flyme2mars.hop.ui.floor.HopCardGap
import com.flyme2mars.hop.ui.floor.PostCard
import com.flyme2mars.hop.ui.theme.HopMotion

@Composable
fun HistoryScreen(
    posts: List<HopPost>,
    motion: HopMotion,
    onOpenPost: (HopPost) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
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
        item(key = "history_header") {
            HopScreenHeader(title = stringResource(R.string.history_title))
        }
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
            )
        }
    }
}
