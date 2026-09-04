package com.flyme2mars.hop.ui.floor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.FrontHand
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.R
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.data.PostKind

val HopCardShape = RoundedCornerShape(18.dp)
val HopCardGap = 12.dp

fun PostKind.icon(): ImageVector = when (this) {
    PostKind.Request -> Icons.Outlined.FrontHand
    PostKind.Offer -> Icons.Outlined.Redeem
    PostKind.Alert -> Icons.Outlined.Campaign
}

@Composable
fun PostKind.label(): String = stringResource(
    when (this) {
        PostKind.Request -> R.string.kind_request
        PostKind.Offer -> R.string.kind_offer
        PostKind.Alert -> R.string.kind_alert
    },
)

@Composable
fun PostCard(
    post: HopPost,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = HopCardShape,
        color = scheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                        shape = RoundedCornerShape(8.dp),
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
