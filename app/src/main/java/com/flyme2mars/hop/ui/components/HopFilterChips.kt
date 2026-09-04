package com.flyme2mars.hop.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.data.PostFilter
import com.flyme2mars.hop.ui.theme.HopDimens
import com.flyme2mars.hop.ui.theme.HopTheme

@Composable
fun HopFilterChips(
    selected: PostFilter,
    onSelect: (PostFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HopDimens.ChipGap),
    ) {
        PostFilter.entries.forEach { filter ->
            HopChip(
                label = filter.label(),
                selected = filter == selected,
                onClick = { onSelect(filter) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun HopChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HopTheme.colors
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = HopDimens.Touch),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (selected) colors.accent else colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) colors.accent else colors.surfaceRaised,
            contentColor = if (selected) colors.accentText else colors.textSecondary,
        ),
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
        )
    }
}

fun PostFilter.label(): String = when (this) {
    PostFilter.All -> "All"
    PostFilter.Offer -> "Offer"
    PostFilter.Ask -> "Ask"
    PostFilter.Note -> "Note"
}
