package com.flyme2mars.hop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.flyme2mars.hop.ui.theme.HopMotion
import com.flyme2mars.hop.ui.theme.HopTokens

@Composable
fun HopSpecularButton(
    label: String,
    onClick: () -> Unit,
    motion: HopMotion,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val specular = if (dark) HopTokens.SpecularDark else HopTokens.SpecularLight
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = HopTokens.Touch)
            .clip(RoundedCornerShape(HopTokens.Radius16))
            .background(if (enabled) scheme.primary else scheme.surfaceContainerHigh)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (!motion.reduced && enabled) {
            SpecularCap(alpha = specular)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) scheme.onPrimary else scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BoxScope.SpecularCap(alpha: Float) {
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .fillMaxHeight(0.30f)
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = alpha), Color.Transparent),
                ),
            ),
    )
}
