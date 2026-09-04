package com.flyme2mars.hop.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.R
import com.flyme2mars.hop.ui.components.HopScreenHeader
import com.flyme2mars.hop.ui.floor.HopCardShape

@Composable
fun SettingsScreen(
    nearbyCount: Int,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
    ) {
        HopScreenHeader(title = stringResource(R.string.settings_title))
        SettingsGroup {
            SettingsRow(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Sensors,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                    )
                },
                title = stringResource(R.string.settings_nearby_title),
                body = pluralStringResource(R.plurals.nearby_count, nearbyCount, nearbyCount),
            )
            SettingsRow(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                    )
                },
                title = stringResource(R.string.settings_demo_title),
                body = stringResource(R.string.settings_demo_body),
            )
        }
        Spacer(Modifier.height(12.dp))
        SettingsGroup {
            SettingsRow(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Contrast,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                    )
                },
                title = stringResource(R.string.settings_theme_title),
                body = stringResource(R.string.settings_theme_body),
            )
            SettingsRow(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                    )
                },
                title = stringResource(R.string.settings_dynamic_title),
                body = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    stringResource(R.string.settings_dynamic_body)
                } else {
                    stringResource(R.string.settings_dynamic_fallback)
                },
            )
        }
        Spacer(Modifier.height(12.dp))
        SettingsGroup {
            SettingsRow(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                    )
                },
                title = stringResource(R.string.settings_about_title),
                body = stringResource(R.string.settings_about_body),
            )
        }
    }
}

@Composable
private fun SettingsGroup(
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = HopCardShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: @Composable () -> Unit,
    title: String,
    body: String,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        icon()
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
