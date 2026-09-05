package com.flyme2mars.hop.ui.post

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.data.PostKind
import com.flyme2mars.hop.ui.components.HopChip
import com.flyme2mars.hop.ui.components.HopFieldSpacer
import com.flyme2mars.hop.ui.components.HopTextField
import com.flyme2mars.hop.ui.components.label
import com.flyme2mars.hop.ui.theme.HopDimens
import com.flyme2mars.hop.ui.theme.HopTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPostSheet(
    onDismiss: () -> Unit,
    onPost: (kind: PostKind, title: String, body: String) -> Unit,
) {
    val colors = HopTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var kind by rememberSaveable { mutableStateOf(PostKind.Offer) }
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        contentColor = colors.textPrimary,
        scrimColor = colors.scrim,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = HopDimens.Side, end = HopDimens.Side, top = HopDimens.SheetTop, bottom = 28.dp),
        ) {
            Text(
                text = "New post",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PostKind.entries.forEach { option ->
                    HopChip(
                        label = option.label(),
                        selected = option == kind,
                        onClick = { kind = option },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            HopTextField(
                label = if (kind == PostKind.Note) "Title (optional)" else "Title",
                value = title,
                onValueChange = { title = it },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
            )
            HopFieldSpacer()
            HopTextField(
                label = "Body",
                value = body,
                onValueChange = { body = it },
                singleLine = false,
                minLines = 3,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    onPost(kind, title, body)
                    onDismiss()
                },
                enabled = body.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HopDimens.Touch),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.accentText,
                    disabledContainerColor = colors.surfaceRaised,
                    disabledContentColor = colors.textSecondary,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Post", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimSheet(
    post: HopPost,
    isOwn: Boolean,
    quieter: Boolean,
    onDismiss: () -> Unit,
    onClaim: () -> Unit,
    onRemove: () -> Unit,
) {
    val colors = HopTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var confirmRemove by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        contentColor = colors.textPrimary,
        scrimColor = colors.scrim,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = HopDimens.Side, end = HopDimens.Side, top = HopDimens.SheetTop, bottom = 28.dp),
        ) {
            Text(
                text = "${post.authorName} · ${post.authorRoom}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = post.kind.label(),
                style = MaterialTheme.typography.labelMedium,
                color = colors.accent,
            )
            if (post.title.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.textPrimary,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(24.dp))
            if (!quieter && !isOwn && post.kind != PostKind.Note && !post.claimed) {
                Button(
                    onClick = {
                        onClaim()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = HopDimens.Touch),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.accentText,
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Claim", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
            }
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HopDimens.Touch),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.surfaceRaised,
                    contentColor = colors.textPrimary,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Close", style = MaterialTheme.typography.titleMedium)
            }
            if (isOwn) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { confirmRemove = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = HopDimens.Touch),
                ) {
                    Text("Remove", color = colors.danger, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    if (confirmRemove) {
        AlertDialog(
            onDismissRequest = { confirmRemove = false },
            title = { Text("Remove this post?") },
            text = { Text("It leaves this floor for everyone here.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmRemove = false
                        onRemove()
                        onDismiss()
                    },
                ) {
                    Text("Remove", color = colors.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmRemove = false }) {
                    Text("Cancel")
                }
            },
            containerColor = colors.surface,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary,
        )
    }
}
