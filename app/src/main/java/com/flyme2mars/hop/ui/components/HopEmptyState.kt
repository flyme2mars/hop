package com.flyme2mars.hop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.flyme2mars.hop.ui.theme.HopCardShape
import com.flyme2mars.hop.ui.theme.HopTokens

@Composable
fun HopEmptyState(
    title: String,
    body: String,
    cta: String,
    onCta: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = HopCardShape,
        color = scheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(HopTokens.CardPaddingWide),
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
            Button(
                onClick = onCta,
                modifier = Modifier.heightIn(min = HopTokens.Touch),
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary,
                ),
            ) {
                Text(text = cta, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
