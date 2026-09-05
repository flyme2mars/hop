package com.flyme2mars.hop.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.ui.components.HopEmptyState
import com.flyme2mars.hop.ui.components.HopPostCard
import com.flyme2mars.hop.ui.theme.HopDimens
import com.flyme2mars.hop.ui.theme.HopTheme

@Composable
fun HistoryScreen(
    posts: List<HopPost>,
    onOpenPost: (HopPost) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val colors = HopTheme.colors
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = HopDimens.Side,
            end = HopDimens.Side,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "History",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary,
            )
        }
        if (posts.isEmpty()) {
            item {
                HopEmptyState(
                    title = "No history yet",
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
        } else {
            items(posts, key = { it.id }) { post ->
                HopPostCard(
                    post = post,
                    onOpen = { onOpenPost(post) },
                    quieter = true,
                    showClaim = false,
                )
            }
        }
    }
}
