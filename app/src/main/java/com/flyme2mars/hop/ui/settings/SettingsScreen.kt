package com.flyme2mars.hop.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.data.HopProfile
import com.flyme2mars.hop.ui.components.HopFieldSpacer
import com.flyme2mars.hop.ui.components.HopRaisedField
import com.flyme2mars.hop.ui.theme.HopDimens
import com.flyme2mars.hop.ui.theme.HopTheme

@Composable
fun SettingsScreen(
    profile: HopProfile,
    keepScreenOn: Boolean,
    onSaveProfile: (name: String, room: String, floor: String) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val colors = HopTheme.colors
    val context = LocalContext.current
    val version = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull().orEmpty().ifBlank { "0.1.0" }

    var name by rememberSaveable(profile.name) { mutableStateOf(profile.name) }
    var room by rememberSaveable(profile.room) { mutableStateOf(profile.room) }
    var floor by rememberSaveable(profile.floor) { mutableStateOf(profile.floor) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = HopDimens.Side,
                end = HopDimens.Side,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp,
            ),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(20.dp))
        HopRaisedField(
            label = "Name",
            value = name,
            onValueChange = { name = it },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
        )
        HopFieldSpacer()
        HopRaisedField(
            label = "Room",
            value = room,
            onValueChange = { room = it },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        HopFieldSpacer()
        HopRaisedField(
            label = "Floor",
            value = floor,
            onValueChange = { floor = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onSaveProfile(name, room, floor) },
            enabled = name.isNotBlank() && room.isNotBlank() && floor.isNotBlank(),
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
            Text("Save", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(28.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HopDimens.Touch),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = "Keep screen on",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
                Text(
                    text = "Blackout stays lit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }
            Switch(
                checked = keepScreenOn,
                onCheckedChange = onKeepScreenOnChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.accentText,
                    checkedTrackColor = colors.accent,
                    uncheckedThumbColor = colors.textSecondary,
                    uncheckedTrackColor = colors.surfaceRaised,
                ),
            )
        }
        Spacer(Modifier.height(32.dp))
        Text(
            text = "About Hop",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Help on this floor stays here when the network is gone.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Version $version",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
    }
}
