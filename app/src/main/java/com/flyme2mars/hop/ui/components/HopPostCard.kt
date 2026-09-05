package com.flyme2mars.hop.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.data.PostKind
import com.flyme2mars.hop.data.formatRelativeTime
import com.flyme2mars.hop.ui.theme.HopDimens
import com.flyme2mars.hop.ui.theme.HopTheme

@Composable
fun HopPostCard(
    post: HopPost,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    quieter: Boolean = false,
    showClaim: Boolean = true,
    onClaim: (() -> Unit)? = null,
) {
    val colors = HopTheme.colors
    val contentAlpha = if (quieter) 0.72f else 1f
    Surface(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HopDimens.CardRadius),
        color = colors.surfaceRaised.copy(alpha = if (quieter) 0.72f else 1f),
        border = BorderStroke(1.dp, colors.border.copy(alpha = if (quieter) 0.7f else 1f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(HopDimens.Card)) {
            Text(
                text = post.kind.label(),
                style = MaterialTheme.typography.labelMedium,
                color = colors.accent.copy(alpha = contentAlpha),
            )
            if (post.title.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary.copy(alpha = contentAlpha),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary.copy(alpha = contentAlpha),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = listOf(
                        post.authorName,
                        post.authorRoom,
                        formatRelativeTime(post.createdAtMillis),
                    ).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary.copy(alpha = contentAlpha),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (showClaim && post.kind != PostKind.Note && onClaim != null) {
                    Button(
                        onClick = onClaim,
                        modifier = Modifier
                            .heightIn(min = HopDimens.Touch)
                            .padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.accentText,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Claim", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

fun PostKind.label(): String = when (this) {
    PostKind.Offer -> "Offer"
    PostKind.Ask -> "Ask"
    PostKind.Note -> "Note"
}
