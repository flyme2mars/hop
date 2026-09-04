package com.flyme2mars.hop.ui.floor

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FrontHand
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.R
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.data.PostKind
import com.flyme2mars.hop.data.hasPrioritySheen
import com.flyme2mars.hop.ui.theme.HopCardShape
import com.flyme2mars.hop.ui.theme.HopTokens
import com.flyme2mars.hop.ui.theme.rememberHopMotion

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
) {
    val scheme = MaterialTheme.colorScheme
    val motion = rememberHopMotion()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (!motion.reduced && pressed) HopTokens.PressScale else 1f,
        animationSpec = motion.press(),
        label = "cardPress",
    )
    val showSheen = !muted && post.kind.hasPrioritySheen

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = HopCardShape,
        color = scheme.surfaceContainer,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        interactionSource = interaction,
    ) {
        Box {
            if (showSheen) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HopTokens.SheenHeight)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    scheme.primary.copy(alpha = HopTokens.SheenAlpha),
                                    Color.Transparent,
                                ),
                            ),
                        ),
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
                        modifier = Modifier.size(18.dp),
                        tint = scheme.onSurfaceVariant,
                    )
                    Text(
                        text = post.kind.label(),
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
                        Surface(
                            shape = RoundedCornerShape(HopTokens.Radius8),
                            color = scheme.secondaryContainer,
                        ) {
                            Text(
                                text = stringResource(R.string.post_claimed),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = scheme.onSecondaryContainer,
                            )
                        }
                    }
                }
                Text(
                    text = post.title,
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
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}
