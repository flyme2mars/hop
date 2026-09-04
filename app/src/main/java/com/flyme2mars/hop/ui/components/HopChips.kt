package com.flyme2mars.hop.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.ui.theme.HopPillShape
import com.flyme2mars.hop.ui.theme.HopTokens
import com.flyme2mars.hop.ui.theme.rememberHopMotion

@Composable
fun HopFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val motion = rememberHopMotion()
    val luminous by animateFloatAsState(
        targetValue = if (selected) HopTokens.ChipLuminousAlpha else 0f,
        animationSpec = motion.chipSelect(),
        label = "chipLuminous",
    )
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .heightIn(min = HopTokens.ChipMinHeight)
            .drawWithContent {
                drawContent()
                if (luminous > 0f) {
                    drawRoundRect(
                        color = scheme.primary.copy(alpha = luminous),
                        cornerRadius = CornerRadius(size.minDimension / 2f),
                    )
                }
            },
        shape = HopPillShape,
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
            containerColor = scheme.surfaceContainer,
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
        modifier = modifier.heightIn(min = HopTokens.ChipMinHeight),
        shape = HopPillShape,
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
            containerColor = scheme.surfaceContainer,
            labelColor = scheme.onSurfaceVariant,
            leadingIconContentColor = scheme.onSurfaceVariant,
        ),
    )
}
