package com.flyme2mars.hop.ui.blackout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.data.formatElapsed
import com.flyme2mars.hop.ui.theme.HopDimens
import com.flyme2mars.hop.ui.theme.HopTheme
import kotlinx.coroutines.delay

enum class BlackoutStatus {
    None,
    Ok,
    Help,
}

@Composable
fun BlackoutScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HopTheme.colors
    val startedAt = remember { System.currentTimeMillis() }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var status by remember { mutableStateOf(BlackoutStatus.None) }

    LaunchedEffect(Unit) {
        while (true) {
            elapsedSeconds = ((System.currentTimeMillis() - startedAt) / 1000L).coerceAtLeast(0)
            delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.blackoutBg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(HopDimens.Touch),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close blackout",
                tint = colors.blackoutAccent,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = HopDimens.Side),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = formatElapsed(elapsedSeconds),
                style = MaterialTheme.typography.displayLarge,
                color = colors.blackoutAccent,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "power cut",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.blackoutAccent.copy(alpha = 0.78f),
            )
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = { status = BlackoutStatus.Ok },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HopDimens.Touch),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.blackoutAccent,
                    contentColor = colors.blackoutBg,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "I'm OK",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { status = BlackoutStatus.Help },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HopDimens.Touch),
                border = BorderStroke(
                    width = if (status == BlackoutStatus.Help) 2.dp else 1.dp,
                    color = colors.blackoutAccent,
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (status == BlackoutStatus.Help) {
                        colors.blackoutAccent.copy(alpha = 0.12f)
                    } else {
                        colors.blackoutBg
                    },
                    contentColor = colors.blackoutAccent,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Need help", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
