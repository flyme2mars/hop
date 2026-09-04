package com.flyme2mars.hop.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun HopFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        label = { Text(text = label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        },
        border = null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = scheme.surfaceContainerHigh,
            labelColor = scheme.onSurface,
            iconColor = scheme.onSurfaceVariant,
            selectedContainerColor = scheme.secondaryContainer,
            selectedLabelColor = scheme.onSecondaryContainer,
            selectedLeadingIconColor = scheme.onSecondaryContainer,
        ),
    )
}

@Composable
fun HopAssistChip(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    AssistChip(
        onClick = onClick,
        modifier = modifier,
        label = { Text(text = label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        },
        border = null,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = scheme.surfaceContainerHigh,
            labelColor = scheme.onSurfaceVariant,
            leadingIconContentColor = scheme.onSurfaceVariant,
        ),
    )
}
