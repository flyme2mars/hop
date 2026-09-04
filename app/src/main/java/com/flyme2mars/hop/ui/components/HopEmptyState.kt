package com.flyme2mars.hop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.ui.theme.HopCardShape
import com.flyme2mars.hop.ui.theme.HopMotion
import com.flyme2mars.hop.ui.theme.HopTokens
import com.flyme2mars.hop.ui.theme.rememberHopMotion

@Composable
fun HopEmptyState(
    title: String,
    body: String,
    cta: String,
    onCta: () -> Unit,
    modifier: Modifier = Modifier,
    motion: HopMotion = rememberHopMotion(),
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = HopCardShape,
        color = scheme.surfaceContainer,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(HopTokens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(HopTokens.Radius8),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = scheme.onSurface,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
            )
            HopSpecularButton(
                label = cta,
                onClick = onCta,
                motion = motion,
            )
        }
    }
}
