package com.flyme2mars.hop.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.R
import com.flyme2mars.hop.ui.floor.HopCardShape

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxSize(),
        color = scheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.onboarding_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = scheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.onboarding_tagline),
                    style = MaterialTheme.typography.titleLarge,
                    color = scheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.onboarding_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                OnboardingCard(
                    icon = Icons.Outlined.Apartment,
                    title = stringResource(R.string.onboarding_floor_title),
                    body = stringResource(R.string.onboarding_floor_body),
                )
                OnboardingCard(
                    icon = Icons.Outlined.Sensors,
                    title = stringResource(R.string.onboarding_nearby_title),
                    body = stringResource(R.string.onboarding_nearby_body),
                )
                OnboardingCard(
                    icon = Icons.Outlined.Schedule,
                    title = stringResource(R.string.onboarding_cut_title),
                    body = stringResource(R.string.onboarding_cut_body),
                )
                Spacer(Modifier.height(8.dp))
            }
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_cta),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Text(
                text = stringResource(R.string.onboarding_footnote),
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OnboardingCard(
    icon: ImageVector,
    title: String,
    body: String,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = HopCardShape,
        color = scheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = scheme.onSurface,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}
