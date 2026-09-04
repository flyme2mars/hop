package com.flyme2mars.hop.ui.launch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
fun LaunchScreen(
    profile: HopProfile,
    onContinue: (name: String, room: String, floor: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HopTheme.colors
    var name by rememberSaveable { mutableStateOf(profile.name) }
    var room by rememberSaveable { mutableStateOf(profile.room) }
    var floor by rememberSaveable { mutableStateOf(profile.floor) }
    val canContinue = name.isNotBlank() && room.isNotBlank() && floor.isNotBlank()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to colors.accent.copy(alpha = 0.22f),
                        0.16f to colors.accent.copy(alpha = 0.10f),
                        0.28f to Color.Transparent,
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 28.dp, end = 0.dp)
                .size(148.dp)
                .background(
                    color = colors.surfaceRaised.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(bottomStart = 48.dp),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.statusBars.union(WindowInsets.navigationBars).union(WindowInsets.ime),
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HopDimens.Side, vertical = 28.dp),
        ) {
            Spacer(Modifier.height(36.dp))
            Text(
                text = "Hop",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Your floor, offline.",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(32.dp))
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
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                ),
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
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = { onContinue(name, room, floor) },
                enabled = canContinue,
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
                Text("Continue", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
