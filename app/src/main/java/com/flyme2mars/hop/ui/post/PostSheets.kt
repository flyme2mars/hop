package com.flyme2mars.hop.ui.post

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.flyme2mars.hop.R
import com.flyme2mars.hop.data.FakeHopRepository
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.data.PostKind
import com.flyme2mars.hop.data.canClaim
import com.flyme2mars.hop.ui.components.HopSpecularButton
import com.flyme2mars.hop.ui.floor.label
import com.flyme2mars.hop.ui.theme.HopMotion
import com.flyme2mars.hop.ui.theme.HopSharedKeys
import com.flyme2mars.hop.ui.theme.HopTokens

@Composable
fun ComposePostContent(
    motion: HopMotion,
    onPublish: (PostKind, String, String) -> Unit,
) {
    var kind by remember { mutableStateOf(PostKind.Offer) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    val canPost = title.trim().isNotEmpty() && body.trim().isNotEmpty()
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(HopTokens.ListGap),
    ) {
        Text(
            text = stringResource(R.string.post_sheet_title),
            style = MaterialTheme.typography.titleLarge,
            color = scheme.onSurface,
        )
        Text(
            text = stringResource(R.string.post_sheet_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = scheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HopTokens.Radius8),
        ) {
            PostKind.entries.forEach { option ->
                val selected = kind == option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = HopTokens.Touch)
                        .clip(RoundedCornerShape(HopTokens.Radius12))
                        .background(
                            if (selected) scheme.surfaceContainerHigh else scheme.surfaceContainer,
                        )
                        .clickable(role = Role.Tab) { kind = option },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = option.label(),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) scheme.onSurface else scheme.onSurfaceVariant,
                    )
                }
            }
        }
        HopSheetField(
            value = title,
            onValueChange = { title = it },
            label = stringResource(R.string.post_field_title),
        )
        HopSheetField(
            value = body,
            onValueChange = { body = it },
            label = stringResource(R.string.post_field_body),
            minLines = 4,
        )
        HopSpecularButton(
            label = stringResource(R.string.post_cta),
            onClick = { onPublish(kind, title, body) },
            motion = motion,
            enabled = canPost,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ColumnScope.ClaimPostContent(
    post: HopPost,
    motion: HopMotion,
    sharedScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onRemove: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val claimedByYou = post.claimedBy == FakeHopRepository.YouName
    var confirmRemove by remember { mutableStateOf(false) }

    with(sharedScope) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(HopTokens.ListGap),
        ) {
            Text(
                text = stringResource(R.string.claim_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                color = scheme.onSurface,
            )
            Text(
                text = post.kind.label(),
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(HopSharedKeys.status(post.id)),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
            Text(
                text = post.title,
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(HopSharedKeys.title(post.id)),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
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
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(HopSharedKeys.room(post.id)),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
            if (post.canClaim()) {
                Text(
                    text = stringResource(R.string.claim_sheet_body, post.author),
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                )
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = HopTokens.Touch),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.primary,
                        contentColor = scheme.onPrimary,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.claim_cta),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            } else if (claimedByYou) {
                TextButton(
                    onClick = {
                        if (confirmRemove) onRemove() else confirmRemove = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = HopTokens.Touch),
                ) {
                    Text(
                        text = stringResource(
                            if (confirmRemove) R.string.claim_remove_confirm else R.string.claim_remove,
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.error,
                    )
                }
            } else {
                Text(
                    text = if (post.claimedBy != null) {
                        stringResource(R.string.post_already_claimed, post.claimedBy)
                    } else {
                        stringResource(R.string.post_detail_no_claim)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                )
            }
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HopTokens.Touch),
            ) {
                Text(
                    text = stringResource(R.string.claim_dismiss),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun HopSheetField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minLines: Int = 1,
) {
    val scheme = MaterialTheme.colorScheme
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        minLines = minLines,
        shape = MaterialTheme.shapes.large,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = scheme.surfaceContainerHighest,
            unfocusedContainerColor = scheme.surfaceContainerHighest,
            disabledContainerColor = scheme.surfaceContainerHighest,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = scheme.primary,
        ),
    )
}
