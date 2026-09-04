package com.flyme2mars.hop.ui.settings

import android.os.Build
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.flyme2mars.hop.R
import com.flyme2mars.hop.ui.theme.HopCardShape
import com.flyme2mars.hop.ui.theme.HopTokens

@Composable
fun SettingsScreen(
    nearbyCount: Int,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val itemColors = ListItemDefaults.colors(containerColor = scheme.surfaceContainer)
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = HopTokens.ScreenGutter)
            .padding(bottom = HopTokens.SectionGap),
    ) {
        SettingsGroup {
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_nearby_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                supportingContent = {
                    Text(
                        text = pluralStringResource(R.plurals.nearby_count, nearbyCount, nearbyCount),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                leadingContent = {
                    Icon(Icons.Outlined.Sensors, contentDescription = null)
                },
                colors = itemColors,
            )
            HorizontalDivider(color = scheme.outlineVariant)
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_demo_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_demo_body),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                leadingContent = {
                    Icon(Icons.Outlined.Info, contentDescription = null)
                },
                colors = itemColors,
            )
        }
        Spacer(Modifier.height(HopTokens.ListGap))
        SettingsGroup {
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_theme_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_theme_body),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                leadingContent = {
                    Icon(Icons.Outlined.Contrast, contentDescription = null)
                },
                colors = itemColors,
            )
            HorizontalDivider(color = scheme.outlineVariant)
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_dynamic_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                supportingContent = {
                    Text(
                        text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            stringResource(R.string.settings_dynamic_body)
                        } else {
                            stringResource(R.string.settings_dynamic_fallback)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                leadingContent = {
                    Icon(Icons.Outlined.Palette, contentDescription = null)
                },
                colors = itemColors,
            )
        }
        Spacer(Modifier.height(HopTokens.ListGap))
        SettingsGroup {
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_about_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_about_body),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                leadingContent = {
                    Icon(Icons.Outlined.Info, contentDescription = null)
                },
                colors = itemColors,
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
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column { content() }
    }
}
