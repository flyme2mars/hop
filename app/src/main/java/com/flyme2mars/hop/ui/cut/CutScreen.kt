package com.flyme2mars.hop.ui.cut

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Sos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.HopAppState
import com.flyme2mars.hop.R
import com.flyme2mars.hop.ui.theme.CutBlack
import com.flyme2mars.hop.ui.theme.CutFlame
import com.flyme2mars.hop.ui.theme.CutOnBlack
import com.flyme2mars.hop.ui.theme.CutOnBlackMuted
import com.flyme2mars.hop.ui.theme.CutOnFlame
import com.flyme2mars.hop.ui.theme.HopCutCtaShape
import com.flyme2mars.hop.ui.theme.HopMotion
import com.flyme2mars.hop.ui.theme.HopTokens
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun CutScreen(
    nearbyCount: Int,
    status: String?,
    motion: HopMotion,
    onOk: () -> Unit,
    onHelp: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onLeave)

    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            now = LocalDateTime.now()
        }
    }

    val timeText = remember(now) { now.format(DateTimeFormatter.ofPattern("HH:mm")) }
    val dateText = remember(now) {
        now.format(DateTimeFormatter.ofPattern("EEEE d MMM", Locale.getDefault()))
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CutBlack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = HopTokens.ScreenGutterWide, vertical = HopTokens.ListGap),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.cut_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    color = CutOnBlack,
                )
                IconButton(
                    onClick = onLeave,
                    modifier = Modifier.size(HopTokens.Touch),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.cd_leave_cut),
                        tint = CutOnBlack,
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(HopTokens.ListGap),
            ) {
                FlameCap(reduced = motion.reduced)
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.displayLarge,
                    color = CutFlame,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = CutOnBlackMuted,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = pluralStringResource(R.plurals.cut_nearby, nearbyCount, nearbyCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = CutOnBlackMuted,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.weight(1f))

            if (status != null) {
                Text(
                    text = when (status) {
                        HopAppState.CUT_HELP ->
                            pluralStringResource(R.plurals.cut_sent_help, nearbyCount, nearbyCount)
                        else ->
                            pluralStringResource(R.plurals.cut_sent_ok, nearbyCount, nearbyCount)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = HopTokens.ListGap),
                    style = MaterialTheme.typography.bodyLarge,
                    color = CutOnBlack,
                    textAlign = TextAlign.Center,
                )
            }

            CutSolidButton(
                text = stringResource(R.string.cut_ok),
                icon = { Icon(Icons.Outlined.Check, contentDescription = null, tint = CutOnFlame) },
                onClick = onOk,
            )
            Spacer(Modifier.height(HopTokens.Radius8 + HopTokens.Radius4))
            CutOutlineButton(
                text = stringResource(R.string.cut_help),
                icon = { Icon(Icons.Outlined.Sos, contentDescription = null, tint = Color.White) },
                onClick = onHelp,
            )
            Spacer(Modifier.height(HopTokens.ListGap))
        }
    }
}

@Composable
private fun FlameCap(reduced: Boolean) {
    val infinite = rememberInfiniteTransition(label = "flameCap")
    val t by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = HopTokens.FlameMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "flameBreath",
    )
    val breath = if (reduced) 1f else t
    val capAlpha = if (reduced) 1f else 0.35f + 0.65f * breath
    val capScaleX = if (reduced) 1f else 0.85f + 0.15f * breath

    Box(
        modifier = Modifier
            .size(HopTokens.FlameGlow * 2)
            .drawBehind {
                val radius = HopTokens.FlameGlow.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CutFlame.copy(alpha = HopTokens.FlameGlowAlpha),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = radius,
                    ),
                    radius = radius,
                    center = center,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(HopTokens.FlameCapWidth, HopTokens.FlameCapHeight)
                .graphicsLayer {
                    scaleX = capScaleX
                    alpha = capAlpha
                }
                .clip(RoundedCornerShape(percent = 50))
                .background(CutFlame),
        )
    }
}

@Composable
private fun CutSolidButton(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HopTokens.Touch)
            .clip(HopCutCtaShape)
            .background(CutFlame)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = HopTokens.CardPadding, vertical = HopTokens.Radius12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        icon()
        Spacer(Modifier.size(HopTokens.Radius8))
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = CutOnFlame)
    }
}

@Composable
private fun CutOutlineButton(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HopTokens.Touch)
            .clip(HopCutCtaShape)
            .border(width = 1.5.dp, color = Color.White, shape = HopCutCtaShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = HopTokens.CardPadding, vertical = HopTokens.Radius12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        icon()
        Spacer(Modifier.size(HopTokens.Radius8))
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}
