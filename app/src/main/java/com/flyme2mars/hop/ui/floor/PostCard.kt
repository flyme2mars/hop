package com.flyme2mars.hop.ui.floor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.FrontHand
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.R
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.data.PostKind
import com.flyme2mars.hop.data.hasLeadingEdge
import com.flyme2mars.hop.ui.theme.HopCardShape
import com.flyme2mars.hop.ui.theme.HopTokens

fun PostKind.icon(): ImageVector = when (this) {
    PostKind.Ask -> Icons.Outlined.FrontHand
    PostKind.Offer -> Icons.Outlined.Redeem
    PostKind.Note -> Icons.AutoMirrored.Outlined.Notes
}

@Composable
fun PostKind.label(): String = stringResource(
    when (this) {
        PostKind.Ask -> R.string.kind_ask
        PostKind.Offer -> R.string.kind_offer
        PostKind.Note -> R.string.kind_note
    },
)

@Composable
fun PostCard(
    post: HopPost,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    muted: Boolean = false,
    titleModifier: Modifier = Modifier,
    roomModifier: Modifier = Modifier,
    statusModifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val showEdge = !muted && post.kind.hasLeadingEdge
    val edgeColor = when (post.kind) {
        PostKind.Offer -> scheme.primary
        PostKind.Ask -> scheme.secondary
        PostKind.Note -> Color.Transparent
    }
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = HopCardShape,
        color = scheme.surfaceContainer,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            if (showEdge) {
                Box(
                    modifier = Modifier
                        .width(HopTokens.LeadingEdge)
                        .fillMaxHeight()
                        .background(edgeColor),
                )
            }
            Column(
                modifier = Modifier.padding(HopTokens.CardPadding),
                verticalArrangement = Arrangement.spacedBy(HopTokens.Radius8),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(HopTokens.Radius8),
                ) {
                    Icon(
                        imageVector = post.kind.icon(),
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                    )
                    Text(
                        text = post.kind.label(),
                        modifier = statusModifier,
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.meta_dot),
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                    )
                    Text(
                        text = post.postedAgo,
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    if (post.claimedBy != null) {
                        Text(
                            text = stringResource(R.string.post_claimed),
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = post.title,
                    modifier = titleModifier,
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface,
                )
                Text(
                    text = post.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.post_meta, post.author, post.place),
                    modifier = roomModifier,
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}
