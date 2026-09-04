package com.flyme2mars.hop.ui.post

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.flyme2mars.hop.R
import com.flyme2mars.hop.data.FakeHopRepository
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.data.PostKind
import com.flyme2mars.hop.data.authorInitials
import com.flyme2mars.hop.data.canClaim
import com.flyme2mars.hop.ui.components.HopFilterChip
import com.flyme2mars.hop.ui.floor.PostCard
import com.flyme2mars.hop.ui.floor.icon
import com.flyme2mars.hop.ui.floor.label
import com.flyme2mars.hop.ui.theme.HopSheetShape
import com.flyme2mars.hop.ui.theme.HopTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposePostSheet(
    onDismiss: () -> Unit,
    onPublish: (PostKind, String, String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var kind by remember { mutableStateOf(PostKind.Ask) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    val canPost = title.trim().isNotEmpty() && body.trim().isNotEmpty()
    val scheme = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = HopSheetShape,
        containerColor = scheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = HopTokens.ScreenGutterWide)
                .padding(bottom = HopTokens.SheetRadius)
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
            Row(horizontalArrangement = Arrangement.spacedBy(HopTokens.Radius8)) {
                PostKind.entries.forEach { option ->
                    HopFilterChip(
                        selected = kind == option,
                        onClick = { kind = option },
                        label = option.label(),
                        icon = option.icon(),
                    )
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
            Spacer(Modifier.height(HopTokens.Radius4))
            Button(
                onClick = { onPublish(kind, title, body) },
                enabled = canPost,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HopTokens.Touch),
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.post_cta),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailSheet(
    post: HopPost,
    onDismiss: () -> Unit,
    onClaim: () -> Unit,
    onRemove: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scheme = MaterialTheme.colorScheme
    val claimedByYou = post.claimedBy == FakeHopRepository.YouName
    var confirmRemove by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = HopSheetShape,
        containerColor = scheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = HopTokens.ScreenGutterWide)
                .padding(bottom = HopTokens.SheetRadius),
            verticalArrangement = Arrangement.spacedBy(HopTokens.CardPadding),
        ) {
            Text(
                text = stringResource(R.string.post_detail_title),
                style = MaterialTheme.typography.titleLarge,
                color = scheme.onSurface,
            )
            PostCard(post = post, onClick = {})
            if (post.canClaim()) {
                Button(
                    onClick = onClaim,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = HopTokens.Touch),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.primary,
                        contentColor = scheme.onPrimary,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.claim_cta_short),
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
                    colors = ButtonDefaults.textButtonColors(contentColor = scheme.error),
                ) {
                    Text(
                        text = stringResource(
                            if (confirmRemove) R.string.claim_remove_confirm else R.string.claim_remove,
                        ),
                        style = MaterialTheme.typography.labelLarge,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimSheet(
    post: HopPost,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scheme = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = HopSheetShape,
        containerColor = scheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = HopTokens.ScreenGutterWide)
                .padding(bottom = HopTokens.SheetRadius),
            verticalArrangement = Arrangement.spacedBy(HopTokens.ListGap),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HopTokens.ListGap),
            ) {
                Surface(
                    modifier = Modifier.size(HopTokens.Avatar),
                    shape = CircleShape,
                    color = scheme.secondaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = authorInitials(post.author),
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSecondaryContainer,
                        )
                    }
                }
                Column {
                    Text(
                        text = post.author,
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurface,
                    )
                    Text(
                        text = post.place,
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = stringResource(R.string.claim_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                color = scheme.onSurface,
            )
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface,
            )
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
